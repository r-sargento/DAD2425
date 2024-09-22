package dadkvs.server;

public class SequencedID {
    private int reqID;
    private int seqID;

    SequencedID (int _reqID, int _seqID){
        reqID = _reqID;
        seqID = _seqID;
    }

    public void set_reqID(int id){
        reqID = id;
    }

    public void set_seqID(int id){
        seqID = id;
    }

    public int get_reqID(){
        return reqID;
    }

    public int get_seqID(){
        return seqID;
    }
}

