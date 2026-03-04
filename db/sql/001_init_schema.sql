-- Initial schema for importing data from alleArten.obj into PostgreSQL.
-- Strategy: keep every import run and link all domain rows to import_run_id.

create table if not exists import_run (
    id bigserial primary key,
    started_at timestamptz not null default now(),
    finished_at timestamptz,
    status text not null,
    source_type text not null,
    source_path text not null,
    source_checksum text,
    parser_version text,
    row_count_taxon integer,
    row_count_synonym integer,
    row_count_taxonomie integer,
    row_count_regelwerk integer,
    row_count_anhang integer,
    row_count_fussnote integer,
    row_count_schutzdetail integer,
    error_count integer not null default 0,
    error_summary text,
    stats_json jsonb
);

create index if not exists idx_import_run_started_at on import_run (started_at desc);
create index if not exists idx_import_run_status on import_run (status);

create table if not exists taxon (
    import_run_id bigint not null,
    knoten_id integer not null,
    wissenschaftlicher_name text,
    gueltiger_name2 text,
    gruppe text,
    deutscher_name text,
    englischer_name text,
    ergaenzende_anmerkung text,
    primary key (import_run_id, knoten_id),
    constraint fk_taxon_import_run
        foreign key (import_run_id)
        references import_run(id)
        on delete cascade
);

create index if not exists idx_taxon_wissenschaftlicher_name on taxon (wissenschaftlicher_name);
create index if not exists idx_taxon_deutscher_name on taxon (deutscher_name);
create index if not exists idx_taxon_gruppe on taxon (gruppe);

create table if not exists taxon_taxonomie (
    import_run_id bigint not null,
    knoten_id integer not null,
    position integer not null,
    element text not null,
    primary key (import_run_id, knoten_id, position),
    constraint fk_taxon_taxonomie_taxon
        foreign key (import_run_id, knoten_id)
        references taxon(import_run_id, knoten_id)
        on delete cascade
);

create table if not exists taxon_synonym (
    import_run_id bigint not null,
    knoten_id integer not null,
    synonym text not null,
    primary key (import_run_id, knoten_id, synonym),
    constraint fk_taxon_synonym_taxon
        foreign key (import_run_id, knoten_id)
        references taxon(import_run_id, knoten_id)
        on delete cascade
);

create index if not exists idx_taxon_synonym_text on taxon_synonym (synonym);

create table if not exists regelwerk (
    import_run_id bigint not null,
    regelwerk_id bigint generated always as identity,
    name text not null,
    primary key (import_run_id, regelwerk_id),
    constraint uq_regelwerk_name_per_run unique (import_run_id, name),
    constraint fk_regelwerk_import_run
        foreign key (import_run_id)
        references import_run(id)
        on delete cascade
);

create table if not exists taxon_regelwerk_name (
    import_run_id bigint not null,
    knoten_id integer not null,
    regelwerk_id bigint not null,
    name_im_regelwerk text not null,
    primary key (import_run_id, knoten_id, regelwerk_id, name_im_regelwerk),
    constraint fk_taxon_regelwerk_name_taxon
        foreign key (import_run_id, knoten_id)
        references taxon(import_run_id, knoten_id)
        on delete cascade,
    constraint fk_taxon_regelwerk_name_regelwerk
        foreign key (import_run_id, regelwerk_id)
        references regelwerk(import_run_id, regelwerk_id)
        on delete cascade
);

create table if not exists anhang (
    import_run_id bigint not null,
    anhang_id bigint generated always as identity,
    regelwerk_id bigint not null,
    name text not null,
    primary key (import_run_id, anhang_id),
    constraint uq_anhang_name_per_regelwerk unique (import_run_id, regelwerk_id, name),
    constraint fk_anhang_regelwerk
        foreign key (import_run_id, regelwerk_id)
        references regelwerk(import_run_id, regelwerk_id)
        on delete cascade
);

create table if not exists taxon_anhang (
    import_run_id bigint not null,
    knoten_id integer not null,
    anhang_id bigint not null,
    name_im_regelwerk text,
    primary key (import_run_id, knoten_id, anhang_id),
    constraint fk_taxon_anhang_taxon
        foreign key (import_run_id, knoten_id)
        references taxon(import_run_id, knoten_id)
        on delete cascade,
    constraint fk_taxon_anhang_anhang
        foreign key (import_run_id, anhang_id)
        references anhang(import_run_id, anhang_id)
        on delete cascade
);

create table if not exists fussnote (
    import_run_id bigint not null,
    fussnote_id text not null,
    text text not null,
    primary key (import_run_id, fussnote_id),
    constraint fk_fussnote_import_run
        foreign key (import_run_id)
        references import_run(id)
        on delete cascade
);

create table if not exists taxon_anhang_fussnote (
    import_run_id bigint not null,
    knoten_id integer not null,
    anhang_id bigint not null,
    fussnote_id text not null,
    primary key (import_run_id, knoten_id, anhang_id, fussnote_id),
    constraint fk_taxon_anhang_fussnote_taxon_anhang
        foreign key (import_run_id, knoten_id, anhang_id)
        references taxon_anhang(import_run_id, knoten_id, anhang_id)
        on delete cascade,
    constraint fk_taxon_anhang_fussnote_fussnote
        foreign key (import_run_id, fussnote_id)
        references fussnote(import_run_id, fussnote_id)
        on delete cascade
);

create table if not exists taxon_schutzdetail (
    import_run_id bigint not null,
    knoten_id integer not null,
    position integer not null,
    unterschutzstellung text not null,
    datum date,
    bemerkung text,
    primary key (import_run_id, knoten_id, position),
    constraint fk_taxon_schutzdetail_taxon
        foreign key (import_run_id, knoten_id)
        references taxon(import_run_id, knoten_id)
        on delete cascade
);
