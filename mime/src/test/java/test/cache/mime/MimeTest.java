package test.cache.mime;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MimeTest {

    @Autowired
    private UniqueMemoryCache cache;
    @Autowired
    private UniquePersistCache persistCache;

    @Test
    public void test() throws InterruptedException {
        Unique unique1 = new Unique(1L, "1", "1");
        cache.clear();

        Assert.assertFalse(cache.has(1));
        cache.set(1, unique1, 50L);
        Thread.sleep(30L);
        Assert.assertTrue(cache.has(1));
        Thread.sleep(30L);
        Assert.assertFalse(cache.has(1));

        cache.set(1, unique1, 50L);
        Thread.sleep(30L);
        Assert.assertTrue(cache.has(1, 50L));
        Thread.sleep(30L);
        Assert.assertTrue(cache.has(1));

        cache.set(1, unique1, 50L);
        Thread.sleep(30L);
        Assert.assertEquals(unique1, cache.get(1));
        Thread.sleep(30L);
        Assert.assertNull(cache.get(1));

        cache.set(1, unique1, 50L);
        Thread.sleep(30L);
        Assert.assertEquals(unique1, cache.get(1, 50L));
        Thread.sleep(30L);
        Assert.assertEquals(unique1, cache.get(1));

        cache.set(1, unique1, 300L);
        cache.set(2, unique1, 300L);
        cache.clear();
        Assert.assertNull(cache.get(1));
        Assert.assertNull(cache.get(2));

    }

    @Test
    public void test2() throws InterruptedException {
        UniquePersistCache cache = persistCache;
        Unique unique1 = new Unique(1L, "1", "1");
        cache.clear();

        Assert.assertFalse(cache.has(1));
        cache.set(1, unique1, 500L);
        Thread.sleep(300L);
        Assert.assertTrue(cache.has(1));
        Thread.sleep(300L);
        Assert.assertFalse(cache.has(1));

        cache.set(1, unique1, 500L);
        Thread.sleep(300L);
        Assert.assertTrue(cache.has(1, 500L));
        Thread.sleep(300L);
        Assert.assertTrue(cache.has(1));

        cache.set(1, unique1, 500L);
        Thread.sleep(300L);
        Assert.assertEquals(unique1, cache.get(1));
        Thread.sleep(300L);
        Assert.assertNull(cache.get(1));

        cache.set(1, unique1, 500L);
        Thread.sleep(300L);
        Assert.assertEquals(unique1, cache.get(1, 500L));
        Thread.sleep(300L);
        Assert.assertEquals(unique1, cache.get(1));

        cache.set(1, unique1, 3000L);
        cache.set(2, unique1, 3000L);
        cache.clear();
        Assert.assertNull(cache.get(1));
        Assert.assertNull(cache.get(2));

        cache.set(1, unique1, 3000L);
        cache.persist(1);
        Thread.sleep(1000L);
        Assert.assertTrue(cache.has(1));
        cache.delete(1);
        Assert.assertFalse(cache.has(1));

    }

}
