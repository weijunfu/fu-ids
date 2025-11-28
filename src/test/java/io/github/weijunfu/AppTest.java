package io.github.weijunfu;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.weijunfu.domain.Amount;
import io.github.weijunfu.domain.Student;
import io.github.weijunfu.id.amount.FuAmountDeserializer;
import io.github.weijunfu.id.amount.FuAmountSerializer;
import io.github.weijunfu.id.util.FuIds;
import io.github.weijunfu.id.util.IdUtil;
import io.github.weijunfu.id.util.Snowflake;
import io.github.weijunfu.id.view.IdView;
import io.github.weijunfu.id.view.IdsView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Unit test for simple App.
 */
@DisplayName("测试")
public class AppTest {

    @Test
    @DisplayName("测试id加解密-hashids")
    void testFuIds() {

        FuIds fuIds = FuIds.getInstance();

        Long id = 1234567L;
        System.out.println("id=" + id);

        String encode = fuIds.encode(id);
        System.out.println("encode: " + encode);

        Long decode = fuIds.decode(encode);
        System.out.println("decode: " + decode);

        assert id.equals(decode);
    }

    @Test
    @DisplayName("测试序列化 & 反序列化")
    void test() throws JsonProcessingException {
        Student student = new Student();
        student.setId(1234567L);
        student.setName("ijunfu");
        student.setIdSchool(987653L);

        System.out.println("student=" + student);

        JsonMapper mapper = new JsonMapper();
        String json = mapper.writeValueAsString(student);
        System.out.println("to json: " + json);

        Student student1 = mapper.readValue(json, Student.class);
        System.out.println("to student: " + student1);
        assert student1.getId().equals(student1.getId());
    }

    @Test
    @DisplayName("集合 & 数组 & Map")
    void testCollectionAndArray() throws JsonProcessingException {

        Student student = new Student();
        student.setId(1234567L);
        student.setName("ijunfu");
        student.setIdSchool(987653L);

        Long[] arr = new Long[4];
        arr[0] = 12345678L;
        arr[1] = 987653L;
        arr[2] = 12345679L;
        arr[3] = 21234567L;
        student.setNums(arr);

        Map<Long, String> map = new HashMap<>();
        map.put(12345678L, "Hello 1");
        map.put(987653L, "Hello 2");
        map.put(12345679L, "Hello 3");
        map.put(21234567L, "Hello 4");
        student.setOthers(map);

        JsonMapper mapper = new JsonMapper();
        String json = mapper.writeValueAsString(student);

        System.out.println(json);

        Student student1 = mapper.readValue(json, Student.class);
        System.out.println("to student: " + student1);
        assert student1.getId().equals(student1.getId());
    }

    @Test
    @DisplayName("id(s) View")
    void testIdView() throws JsonProcessingException {
        JsonMapper mapper = new JsonMapper();

        IdView idView = new IdView(1L);
        System.out.println(mapper.writeValueAsString(idView));  // {"id":"jR"}

        IdsView idsView = new IdsView(List.of(1L, 2L, 3L));
        System.out.println(mapper.writeValueAsString(idsView)); // {"ids":["jR","k5","l5"]}
    }

    @Test
    @DisplayName("金额")
    void testAmount() throws JsonProcessingException {
        Amount amount = new Amount();
        amount.setAmount1(0.244);
        amount.setAmount2(0.247);

        amount.setAmount3(0.4844);
        amount.setAmount4(0.4885);

        JsonMapper mapper = new JsonMapper();

        // 添加此行来查看详细日志
        mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);

        // 注册自定义模块
        /* SimpleModule module = new SimpleModule();
        module.addSerializer(Double.class, new FuAmountSerializer());
        module.addDeserializer(Double.class, new FuAmountDeserializer());
        mapper.registerModule(module); */

        String json = mapper.writeValueAsString(amount);

        System.out.println(json);

        Amount amount1 = mapper.readValue(json, Amount.class);
        System.out.println(amount1.toString());
    }

    @Test
    void testSnowflake() {
        Snowflake snowflake = IdUtil.getSnowflake(1, 5);

        for (int i = 0; i < 100; i++) {
            System.out.println(snowflake.nextId());
        }
    }

    @Test
    void testSnowflakePool() throws Exception {
        int threadCount = 50;
        int totalRequests = 500000; // 共 50 万 ID
        Snowflake snowflake = new Snowflake(1, 1);

        Set<Long> ids = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                for (int j = 0; j < totalRequests / threadCount; j++) {
                    long id = snowflake.nextId();
                    ids.add(id);
                }
                latch.countDown();
            });
        }

        latch.await();
        long end = System.currentTimeMillis();

        System.out.println("生成 ID 数量: " + ids.size());
        System.out.println("耗时(ms): " + (end - start));
        System.out.println("QPS: " + (ids.size() * 1000L / (end - start)));

        pool.shutdown();
    }
}
