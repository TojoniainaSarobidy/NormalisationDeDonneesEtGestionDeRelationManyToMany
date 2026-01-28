import java.util.List;
import java.util.Objects;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private Double price;
    private List<DishIngredient> ingredients;

    public Dish() {}

    public Dish(Integer id, String name, DishTypeEnum dishType, Double price, List<DishIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.price = price;
        setIngredients(ingredients);
    }

    public Double getDishCost() {
        if (ingredients == null) return 0.0;

        double total = 0;
        for (DishIngredient di : ingredients) {
            if (di.getQuantityRequired() == null || di.getIngredient() == null) {
                throw new RuntimeException("Invalid dish ingredient");
            }
            total += di.getIngredient().getPrice() * di.getQuantityRequired();
        }
        return total;
    }

    public Double getGrossMargin() {
        if (price == null) {
            throw new RuntimeException("Dish price is null");
        }
        return price - getDishCost();
    }

    public void setIngredients(List<DishIngredient> ingredients) {
        this.ingredients = ingredients;
        if (ingredients != null) {
            for (DishIngredient di : ingredients) {
                di.setDish(this);
            }
        }
    }

    public List<DishIngredient> getIngredients() {
        return ingredients;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dish)) return false;
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", price=" + price +
                ", ingredients=" + ingredients +
                '}';
    }
}
