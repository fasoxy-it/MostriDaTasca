package it.mattiafasoli.mostridatasca;

public class MonsterCandy {
    private String id;
    private Double lat;
    private Double lon;
    private String type;
    private String size;
    private String name;

    public MonsterCandy (String id, Double lat, Double lon, String type, String size, String name) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
        this.size = size;
        this.name = name;
    }

    public String getMonsterCandyId() {
        return id;
    }

    public Double getMonsterCandyLat() {return lat;}

    public Double getMonsterCandyLon() {return lon;}

    public String getMonsterCandyType() {return type;}

    public String getMonsterCandySize() {return size;}

    public String getMonsterCandyName() {return name;}

}
