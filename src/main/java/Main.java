import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

 /* public class Main {

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
} */


/* import java.time.Instant;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        DataRetriever dataRetriever = new DataRetriever();

        try {
            System.out.println("AVANT COMMANDE");

            dataRetriever.findStockMovementByIngredientId(1)
                    .forEach(System.out::println);

            Dish dish = dataRetriever.findDishById(1);

            DishOrder dishOrder = new DishOrder();
            dishOrder.setDish(dish);
            dishOrder.setQuantity(2);

            Order order = new Order();
            order.setReference("CMD_" + System.currentTimeMillis());
            order.setCreationDatetime(Instant.now());
            order.setDishOrders(List.of(dishOrder));

            Order savedOrder = dataRetriever.saveOrder(order);

            System.out.println("Commande enregistrée avec succès");
            System.out.println(savedOrder);

            System.out.println("APRÈS COMMANDE");

            dataRetriever.findStockMovementByIngredientId(1)
                    .forEach(System.out::println);

            Order foundOrder = dataRetriever.findOrderByReference(savedOrder.getReference());
            System.out.println("\n🔍 Commande retrouvée");
            System.out.println(foundOrder);

        } catch (RuntimeException e) {
            System.err.println("Erreur : " + e.getMessage());
        } */


public class Main {
    public static void main(String[] args) {


        // Initial stock en KG
        double stockLaitue = 5.0;
        double stockTomate = 4.0;
        double stockPoulet = 10.0;
        double stockChocolat = 3.0;
        double stockBeurre = 2.5;

        // Création des ingrédients (simulé)
        Ingredient laitue = new Ingredient(1, "Laitue", 0.0, null);
        Ingredient tomate = new Ingredient(2, "Tomate", 0.0, null);
        Ingredient poulet = new Ingredient(3, "Poulet", 0.0, null);
        Ingredient chocolat = new Ingredient(4, "Chocolat", 0.0, null);
        Ingredient beurre = new Ingredient(5, "Beurre", 0.0, null);

        // Nouveaux mouvements à appliquer
        List<StockMovement> movements = new ArrayList<>();
        movements.add(

                createOutMovement(tomate, 5, "PCS"));
        movements.add(

                createOutMovement(laitue, 2, "PCS"));
        movements.add(

                createOutMovement(chocolat, 1, "L"));
        movements.add(

                createOutMovement(poulet, 4, "PCS"));
        movements.add(

                createOutMovement(beurre, 1, "L"));

        // Calcul du stock final
        for (
                StockMovement m : movements) {
            double qty = DataRetriever.UnitConverter.convertToReference(
                    m.getIngredient().getName(),
                    m.getValue().getQuantity(),
                    m.getValue().getUnit()
            );

            switch (m.getIngredient().getName()) {
                case "Laitue" -> stockLaitue -= qty;
                case "Tomate" -> stockTomate -= qty;
                case "Poulet" -> stockPoulet -= qty;
                case "Chocolat" -> stockChocolat -= qty;
                case "Beurre" -> stockBeurre -= qty;
            }
        }

        // Affichage du résultat
        System.out.println("Stock final :");
        System.out.printf("Laitue   : %.1f KG%n", stockLaitue);
        System.out.printf("Tomate   : %.1f KG%n", stockTomate);
        System.out.printf("Poulet   : %.1f KG%n", stockPoulet);
        System.out.printf("Chocolat : %.1f KG%n", stockChocolat);
        System.out.printf("Beurre   : %.1f KG%n", stockBeurre);
    }

    private static StockMovement createOutMovement(Ingredient ingredient, double qty, String unit) {
        StockMovement sm = new StockMovement();
        sm.setIngredient(ingredient);
        sm.setType(StockMovement.MovementTypeEnum.OUT);

        StockValue sv = new StockValue();
        sv.setQuantity(qty);
        sv.setUnit(unit);
        sm.setValue(sv);

        sm.setCreationDatetime(Instant.now());
        return sm;
    }
}