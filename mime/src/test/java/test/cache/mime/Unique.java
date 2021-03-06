package test.cache.mime;

import java.io.Serializable;
import java.util.Objects;

public class Unique implements Serializable {

    private static final long serialVersionUID = 7042334086334103024L;

    private Long id;
    private String pass;
    private String name;

    public Unique(Long id, String pass, String name) {
        this.id = id;
        this.pass = pass;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Unique unique = (Unique) o;
        return Objects.equals(id, unique.id) &&
                Objects.equals(pass, unique.pass) &&
                Objects.equals(name, unique.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pass, name);
    }

    @Override
    public String toString() {
        return "Unique{" +
                "id=" + id +
                ", pass='" + pass + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
