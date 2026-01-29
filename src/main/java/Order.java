import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {
    private int id;
    private String reference;
    private Instant creationDatetime;

    private Integer tableId;
    private Instant installationDatetime;
    private Instant departureDatetime;

    private List<DishOrder> dishOrders;

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Instant getInstallationDatetime() {
        return installationDatetime;
    }

    public void setInstallationDatetime(Instant installationDatetime) {
        this.installationDatetime = installationDatetime;
    }

    public Instant getDepartureDatetime() {
        return departureDatetime;
    }

    public void setDepartureDatetime(Instant departureDatetime) {
        this.departureDatetime = departureDatetime;
    }

    public Order() {
    }

    public Order(int id, String reference, Instant creationDatetime, List<DishOrder> dishOrders) {
        this.id = id;
        this.reference = reference;
        this.creationDatetime = creationDatetime;
        this.dishOrders = dishOrders;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    public Double getTotalAmountWithoutVAT() {
        if (dishOrders == null || dishOrders.isEmpty()) {
            return 0.0;
        }

        return dishOrders.stream()
                .filter(d -> d.getDish() != null && d.getDish().getPrice() != null)
                .mapToDouble(d ->
                        d.getDish().getPrice() * d.getQuantity()
                )
                .sum();
    }

    public Double getTotalAmountWithVAT() {
        final double VAT_RATE = 0.20;

        Double totalHT = getTotalAmountWithoutVAT();
        return totalHT + (totalHT * VAT_RATE);
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", creationDatetime=" + creationDatetime +
                ", dishOrders=" + dishOrders +
                '}';
    }
}
