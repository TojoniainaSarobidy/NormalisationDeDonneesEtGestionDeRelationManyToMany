import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Table {
    private int id;
    private int number;
    List<Order> orders;

    public Table() {
    }

    public Table(int id, int number, List<Order> orders) {
        this.id = id;
        this.number = number;
        this.orders = orders;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public boolean isAvalaibleAt(Instant t) {
        if (orders == null || orders.isEmpty()) {
            return true;
        }

        return orders.stream().noneMatch(order ->
                order.getInstallationDatetime() != null
                        && order.getDepartureDatetime() != null
                        && !t.isBefore(order.getInstallationDatetime())
                        && t.isBefore(order.getDepartureDatetime())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return id == table.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", number=" + number +
                ", orders=" + orders +
                '}';
    }
}