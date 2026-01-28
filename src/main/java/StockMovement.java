import java.time.Instant;

public class StockMovement {
    private Integer id;
    private StockValue value;
    private Ingredient ingredient;

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    private MovementTypeEnum type;
    private Instant creationDatetime;

    public Integer getId() {
        return id;
    }

    public StockValue getValue() {
        return value;
    }

    public void setValue(StockValue value) {
        this.value = value;
    }

    public MovementTypeEnum getType() {
        return type;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setType(MovementTypeEnum type) {
        this.type = type;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public enum MovementTypeEnum {
        IN, OUT
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + id +
                ", value=" + value +
                ", type=" + type +
                ", creationDatetime=" + creationDatetime +
                '}';
    }
}