package io.github.weijunfu;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.weijunfu.domain.Student;
import io.github.weijunfu.id.util.FuIds;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        student.setName("test");
        System.out.println("student=" + student);

        JsonMapper mapper = new JsonMapper();
        String json = mapper.writeValueAsString(student);
        System.out.println("to json: " + json);

        Student student1 = mapper.readValue(json, Student.class);
        System.out.println("to student: " + student1);
        assert student1.getId().equals(student1.getId());
    }
}
