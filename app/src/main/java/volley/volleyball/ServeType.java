package volley.volleyball;


public enum ServeType {

    SERVE(0, R.string.serve_action),
    ATTACK(1, R.string.attack_action),
    BLOCK(2, R.string.block_action),
    PASS(3, R.string.pass_action);

    private final int value;
    private final int stringId;

    private ServeType(final int value, final int stringId) {
        this.value = value;
        this.stringId = stringId;
    }

    public int getValue() {
        return value;
    }

    public int getStringId(){
        return stringId;
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
