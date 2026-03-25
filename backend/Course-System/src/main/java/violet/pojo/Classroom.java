package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教室实体类，对应表：classroom。
 * <p>
 * 字段采用驼峰命名，便于 MyBatis 通过别名映射。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//第一个是提供get set方法，第二个是有参构造，第三个是无参构造
public class Classroom {

    /**
     * 教室编号（Primary Key）
     */
    private Integer classroomId;

    /**
     * 教室类型：普通教室/计算机机房/实验室
     */
    private String classroomType;

    /**
     * 教室容量
     */
    private Integer classroomCap;

    /**
     * 教室状态：可用/被占用/维修
     */
    private String classroomState;

    /**
     * 教室名称
     */
    private String classroomName;
}

