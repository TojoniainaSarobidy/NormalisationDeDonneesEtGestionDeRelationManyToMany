import java.util.List;

public class Main {

    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        try {
            Ingredient ingredient1 = new Ingredient();
            ingredient1.setName("Concombre");
            ingredient1.setCategory(CategoryEnum.VEGETABLE);
            ingredient1.setPrice(700.0);

            Ingredient ingredient2 = new Ingredient();
            ingredient2.setName("Fromage Feta");
            ingredient2.setCategory(CategoryEnum.DAIRY);
            ingredient2.setPrice(1500.0);

            List<Ingredient> newIngredients = List.of(ingredient1, ingredient2);
            List<Ingredient> savedIngredients = dataRetriever.createIngredients(newIngredients);

            System.out.println("=== INGREDIENTS SAUVEGARDÉS ===");
            for (Ingredient ing : savedIngredients) {
                System.out.println(
                        "ID: " + ing.getId() +
                                " | Name: " + ing.getName() +
                                " | Category: " + ing.getCategory() +
                                " | Price: " + ing.getPrice()
                );
            }

            Dish dish = new Dish();
            dish.setName("Salade test JDBC");
            dish.setDishType(DishTypeEnum.START);
            dish.setPrice(4000.0);

            DishIngredient di1 = new DishIngredient();
            di1.setIngredient(savedIngredients.get(0));
            di1.setQuantityRequired(0.25);
            di1.setUnit(UnitTypeEnum.KG);

            DishIngredient di2 = new DishIngredient();
            di2.setIngredient(savedIngredients.get(1));
            di2.setQuantityRequired(0.15);
            di2.setUnit(UnitTypeEnum.KG);

            dish.setIngredients(List.of(di1, di2));
            Dish savedDish = dataRetriever.saveDish(dish);

            System.out.println("\n=== PLAT SAUVEGARDÉ ===");
            System.out.println("ID      : " + savedDish.getId());
            System.out.println("Name    : " + savedDish.getName());
            System.out.println("Type    : " + savedDish.getDishType());
            System.out.println("Price   : " + savedDish.getPrice());

            System.out.println("\n=== INGREDIENTS DU PLAT ===");
            for (DishIngredient di : savedDish.getIngredients()) {
                System.out.println(
                        "- " + di.getIngredient().getName() +
                                " | Quantity: " + di.getQuantityRequired() +
                                " | Unit: " + di.getUnit() +
                                " | Price/unit: " + di.getIngredient().getPrice()
                );
            }

            System.out.println("\n=== COST & MARGIN ===");
            System.out.println("Dish cost    : " + savedDish.getDishCost());
            System.out.println("Gross margin : " + savedDish.getGrossMargin());

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}