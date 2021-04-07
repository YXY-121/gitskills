/**
 * @author: yxy
 * Date: 2021/4/7
 * Time: 9:55
 * 描述:
 */
public class master {
    public void test(){
        System.out.println("我是master");
        System.out.println("dev修改");


       System.out.println("解决冲突");
       

       System.out.println("尝试合并之后不让dev的信息丢失 ");//git merge --no-ff -m "merge with no-ff" dev

        System.out.println("尝试合并之后让dev的信息丢失 ");//git merge --no-ff -m "merge with no-ff" dev

    }
}
