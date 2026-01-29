import java.time.Instant;

public class TableOrder {
    private Table table;
    private Instant arrivalDateTime;
    private Instant departureDatetime;

    public TableOrder(Table table, Instant arrivalDateTime, Instant departureDatetime) {
        this.table = table;
        this.arrivalDateTime = arrivalDateTime;
        this.departureDatetime = departureDatetime;
    }
}
