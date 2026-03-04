package deringo.wisia.rest;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import deringo.wisia.art.Art;
import deringo.wisia.rest.error.ApiError;
import deringo.wisia.rest.error.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/arten")
@Validated
@Tag(name = "Arten", description = "Read API fuer Arten aus alleArten.obj")
public class ArtController {
    private final ArtReadService artReadService;

    public ArtController(ArtReadService artReadService) {
        this.artReadService = artReadService;
    }

    @Operation(summary = "Liefert eine Art per knotenId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Art gefunden"),
            @ApiResponse(responseCode = "404", description = "Art nicht gefunden", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{knotenId}")
    public Art getByKnotenId(@PathVariable int knotenId) {
        return artReadService.getByKnotenId(knotenId)
                .orElseThrow(() -> new ResourceNotFoundException("Art mit knotenId " + knotenId + " nicht gefunden."));
    }

    @Operation(summary = "Sucht Arten mit optionalen Filtern und Paging")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trefferliste"),
            @ApiResponse(responseCode = "400", description = "Ungueltige Anfrageparameter", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ArtListResponse search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gruppe,
            @RequestParam(required = false) String regelwerk,
            @RequestParam(required = false) String anhang,
            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset) {
        ArtReadService.SearchResult result = artReadService.search(name, gruppe, regelwerk, anhang, limit, offset);
        return new ArtListResponse(result.items(), result.total(), result.limit(), result.offset());
    }

    public record ArtListResponse(List<Art> items, int total, int limit, int offset) {
    }
}
