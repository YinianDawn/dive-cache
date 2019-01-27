package test.cache.ehcache;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EhcacheTest {

    @Autowired
    private UniqueCache cache;

    @Test
    public void test() {
        Unique unique1 = new Unique(1L, "1", "1");
        cache.clear();

        Assert.assertFalse(cache.has(1L));
        cache.set(1L, unique1);
        Assert.assertTrue(cache.has(1L));
        cache.delete(1L);
        Assert.assertFalse(cache.has(1L));

        cache.set(1L, unique1);
        Assert.assertEquals(unique1, cache.get(1L));
        cache.delete(1L);
        Assert.assertNull(cache.get(1L));

        cache.set(1L, unique1);
        Assert.assertEquals(unique1, cache.get(1L));
        cache.remove(1L);
        Assert.assertNull(cache.get(1L));

        cache.set(1L, unique1);
        cache.set(2L, unique1);
        Assert.assertEquals(unique1, cache.get(1L));
        Assert.assertEquals(unique1, cache.get(2L));
        cache.remove(1L);
        cache.remove(2L);
        Assert.assertNull(cache.get(1L));
        Assert.assertNull(cache.get(2L));

        cache.set(1L, unique1);
        cache.set(2L, unique1);
        Assert.assertEquals(unique1, cache.get(1L));
        Assert.assertEquals(unique1, cache.get(2L));
        cache.clear();
        Assert.assertNull(cache.get(1L));
        Assert.assertNull(cache.get(2L));

    }

}
