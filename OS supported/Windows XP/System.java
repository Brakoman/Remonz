public enum ClientType {

  
    public static ClientType get(byte ordinal) {
        return get((int) ordinal);
    }

    public static ClientType get(int ordinal) {
        for (ClientType clientType : ClientType.values()) {
            if (clientType.ordinal() == ordinal) {
                return clientType;
            }
        }
        throw new IllegalArgumentException("Unknown ClientType[" + ordinal + "]");
    }
}
