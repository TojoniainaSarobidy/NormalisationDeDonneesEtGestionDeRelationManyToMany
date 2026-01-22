import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("** TESTS additionnels getDishCost() et getGrossMargin() ****\n");

        System.out.println("A) getDishCost() et getGrossMargin() pour plat existant (ID=1) :");
        try {
            Dish d1 = dataRetriever.findDishById(1);
            System.out.println("Plat: " + d1.getName());
            System.out.println("Coût des ingrédients (getDishCost): " + d1.getDishCost());
            try {
                System.out.println("Marge brute (getGrossMargin): " + d1.getGrossMargin());
            } catch (RuntimeException e) {
                System.out.println("getGrossMargin a levé RuntimeException: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        System.out.println();

        System.out.println("B) getDishCost() pour plat sans prix (ID=3) et vérification d'exception pour getGrossMargin() :");
        try {
            Dish d3 = dataRetriever.findDishById(3);
            System.out.println("Plat: " + d3.getName());
            System.out.println("Coût des ingrédients (getDishCost): " + d3.getDishCost());
            try {
                double margin = d3.getGrossMargin();
                System.out.println("ERREUR: getGrossMargin n'a pas levé d'exception et a retourné: " + margin);
            } catch (RuntimeException e) {
                System.out.println("getGrossMargin a bien levé RuntimeException: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        System.out.println();

        System.out.println("C) Plat sans ingrédients : coût attendu = 0, marge = prix");
        try {
            Dish temp = new Dish(0, "Plat sans ingr", DishTypeEnum.START, 1000.0, new ArrayList<>());
            Dish savedTemp = dataRetriever.saveDish(temp);
            System.out.println("Plat créé: " + savedTemp.getName() + " (ID: " + savedTemp.getId() + ")");
            System.out.println("Coût des ingrédients (getDishCost): " + savedTemp.getDishCost());
            System.out.println("Marge brute (getGrossMargin): " + savedTemp.getGrossMargin());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        System.out.println();
    }
}