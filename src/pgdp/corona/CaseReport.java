package pgdp.corona;

//A CaseReport object represents a reported infection/death/recovery case in an Entry
public class CaseReport {
    public final int count;
    public final Type type;

    public CaseReport(int count, Type type) {
        this.count = count;
        this.type = type;
    }

    @Override
    public String toString() {
        return "{count=" + count + ", type=" + type + "}";
    }

    //Type defines what the count attribute means
    public enum Type {
        NEW, NOT_NEW, CORRECTION;

        public static CaseReport.Type fromDataset(int value) {
            return switch (value) {
                case 1 -> NEW;
                case 0, -9 -> NOT_NEW;
                case -1 -> CORRECTION;
                default -> throw new IllegalArgumentException("Unknown Type:" + value);
            };
        }
    }

    //Getter interface for getters in Entry that return CaseReport (getInfection, getDeath, getRecovery)
    public interface Getter {
        CaseReport get(Entry entry);
    }
}