package test.cache.guava;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GuavaTest {

    @Autowired
    private UniqueCache cache;

    @Test
    public void test() {
        Unique unique1 = new Unique(1L, "1", "1");
        cache.clear();

        Assert.assertFalse(cache.has(1));
        cache.set(1, unique1);
        Assert.assertTrue(cache.has(1));
        cache.delete(1);
        Assert.assertFalse(cache.has(1));

        cache.set(1, unique1);
        Assert.assertEquals(unique1, cache.get(1));
        cache.delete(1);
        Assert.assertNull(cache.get(1));

        cache.set(1, unique1);
        Assert.assertEquals(unique1, cache.get(1));
        cache.remove(1);
        Assert.assertNull(cache.get(1));

        cache.set(1, unique1);
        cache.set(2, unique1);
        Assert.assertEquals(unique1, cache.get(1));
        Assert.assertEquals(unique1, cache.get(2));
        cache.remove(1);
        cache.remove(2);
        Assert.assertNull(cache.get(1));
        Assert.assertNull(cache.get(2));

        cache.set(1, unique1);
        cache.set(2, unique1);
        Assert.assertEquals(unique1, cache.get(1));
        Assert.assertEquals(unique1, cache.get(2));
        cache.clear();
        Assert.assertNull(cache.get(1));
        Assert.assertNull(cache.get(2));

    }

}
