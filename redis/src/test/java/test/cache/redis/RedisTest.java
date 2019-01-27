package test.cache.redis;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private UniqueCache cache;

    @Test
    public void test() throws InterruptedException {
        Unique unique1 = new Unique(1L, "1", "1");
        cache.clear();

        Assert.assertFalse(cache.has("1"));
        cache.set("1", unique1, 5000L);
        Thread.sleep(3000L);
        Assert.assertTrue(cache.has("1"));
        Thread.sleep(3000L);
        Assert.assertFalse(cache.has("1"));

        cache.set("1", unique1, 5000L);
        Thread.sleep(3000L);
        Assert.assertTrue(cache.has("1", 5000L));
        Thread.sleep(3000L);
        Assert.assertTrue(cache.has("1"));

        cache.set("1", unique1, 5000L);
        Thread.sleep(3000L);
        Assert.assertEquals(unique1, cache.get("1"));
        Thread.sleep(3000L);
        Assert.assertNull(cache.get("1"));

        cache.set("1", unique1, 5000L);
        Thread.sleep(3000L);
        Assert.assertEquals(unique1, cache.get("1", 5000L));
        Thread.sleep(3000L);
        Assert.assertEquals(unique1, cache.get("1"));

        cache.set("1", unique1, 30000L);
        cache.set("2", unique1, 30000L);
        cache.clear();
        Assert.assertEquals(unique1, cache.get("1"));
        Assert.assertEquals(unique1, cache.get("2"));

        cache.set("1", unique1, 30000L);
        cache.persist("1");
        Thread.sleep(10000L);
        Assert.assertTrue(cache.has("1"));
        cache.delete("1");
        Assert.assertFalse(cache.has("1"));

    }

}
