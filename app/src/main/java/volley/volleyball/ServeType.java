package volley.volleyball;


public enum ServeType {

    SERVE(0),
    ATTACK(1),
    BLOCK(2),
    PASS(3);

    private int value;

    private ServeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ServeType valueOf(final int value){
        for(ServeType type : values()){
            if(type.getValue() == value){
                return type;
            }
        }
        return null;
    }
}
