package item.model.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BuyItemRequestDto {
    private int playerId;
    private int itemId;
}
