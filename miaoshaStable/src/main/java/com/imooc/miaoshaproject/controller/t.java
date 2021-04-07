package com.imooc.miaoshaproject.controller;

import com.mysql.cj.x.protobuf.MysqlxDatatypes;

import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

/**
 * @author: yxy
 * Date: 2021/3/3
 * Time: 19:59
 * 描述:
 */
public class t {
    public static void main(String args[]) {
        Stack stack=new Stack<>();
        int[] arr = {1,2,3,4,5};
        HashMap<Integer,Integer>a=new  HashMap<Integer,Integer>();

        int n = arr.length, index = 0;
        while (true) {
            System.out.println(arr[index % n]);
            index++;
        }


    }
    int count=0;
    public static int f(int a,int b){
        for(int i=0;i<=b;i++)
            if(b%i==0)
                //count++;
           return 0;
            return 1;
    }
    public static int get(int p,int v){
        int  count=0;
        while(p%v!=0){
            v=p%v;
            count++;
        }
        return count+1;
    }
    public static int maxProfit(int[] prices) {
        int len=prices.length;
        int lowPrices[]=new int[len];
        int tmp=Integer.MAX_VALUE;
        //获得每一天前的最小价格
        for(int i=0;i<len;i++){
            lowPrices[i]=Math.min(tmp,prices[i]);
            tmp=lowPrices[i];
        }



        int profits[]=new int [len];//每一天的最大利润
        tmp=Integer.MIN_VALUE;
        for(int i=1;i<len;i++){
            profits[i]=Math.max(profits[i-1],prices[i]-lowPrices[i-1]);//为什么是i-1 不是i呢
            System.out.println("i"+i+"profit"+profits[i]+"lowPrices[i-1]"+lowPrices[i-1]);
        }
        return profits[len-1];


    }
    public static int trap(int[] height) {
        int len=height.length;
        int n=0,m=len;
        for(int i=0;i<len;i++){
            n=Math.max(n,height[i]);
        }
        System.out.println("n"+n);
        int dp[][]=new int[m][n];
        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++){
                if(height[i]>0)
                {
                    dp[i][j]++;
                    height[i]--;
                }else{
                    break;
                }

            }
        }


        int count=0;
        for(int j=0;j<n;j++){
            boolean found1=false;
            boolean found2=false;
            int flag=0;
            for(int i=0;i<m;i++){
                System.out.println("j是"+j+"i是"+i+"值是"+dp[i][j]);
                if(found1==false&&dp[i][j]==1){
                    found1=true;
                    flag=i;
                    System.out.println("flag"+flag);

                }

                if(found1==true&&dp[i][j]==0&&i+1<m&&dp[i+1][j]==1){
                    found2=true;

                   /* count++;
                    System.out.println("    发现count"+count);*/

                }
                if(found1&&found2){
                  int tmp=i-flag-1;
                  count=count+tmp;
                    System.out.println("    发现count"+count);
                    found1=false;
                    found2=false;
               }
            }
        }
        return count;
    }
    public int search(int[] nums, int target) {
        //找到反转的k值 如果k=0，那就是不反转
        int tmp=nums[0];
        int k=0;
        for(int i=1;i<nums.length;i++){
            if(nums[i]<tmp)
            {
                k=i-1;
                break;
            }
            tmp=nums[i];
        }
        int left=0,right=nums.length-1;
        if(k!=0&&target>nums[nums.length-1])
        {
            right=nums[k];
            left=0;

        }
        else if(k!=0&&target<=nums[nums.length-1]){
            left=k+1;
        }


        while(left<right){
            int mid=left+(right-left)/2;

            if(target<nums[mid]){
                right=mid;
            }
            else{
                left=mid+1;
            }
        }
        return left;

    }

    public static int search1(int[] nums, int target) {
        //找到反转的k值 如果k=0，那就是不反转
        int tmp=nums[0];
        int k=0;
        for(int i=1;i<nums.length;i++){
            if(nums[i]<tmp)
            {
                k=i-1;
                break;
            }
            tmp=nums[i];
        }
        System.out.println("k是"+k);
        int left=0,right=nums.length-1;
        if(k!=0)
        {
            if(target>nums[nums.length-1])
            right=nums[k];
            if(target<=nums[nums.length-1]){
                left=nums[k+1];
            }

        }


        System.out.println("left"+left+"right"+right);

        while(left<right){
            int mid=left+(right-left)/2;

            if(target<nums[mid]){
                right=mid;
            }
            else{
                left=mid+1;
            }
        }
        return left;

    }
    public static int numberOfSubarrays(int[] nums, int k) {
        ////////正常滑动窗口/////////////////////////////////
        int left = 0, right = 0;//左边用来计算遇到第一个奇数前的偶数个数。右边用来计算遇到第k个值。以及，第K+1个奇数和第K个奇数之间的偶数个数
        int len = nums.length;
        int res = 0;
        int odd = 0;
        while (right < len) {
            int rightflag = 0;
            while (right < len && odd < k) {
                int tmp = nums[right];
                if (tmp%2!=0) {
                    odd++;
                    System.out.println("odd"+odd+" "+"right"+right);
                }
                if (odd == k)
                {
                    rightflag = right;
                    System.out.println("odd=k时，right："+right);
                }


                right++;
            }
            System.out.println("第一个while循环后的right"+right);
            if (right >= len && odd < k)
                return 0;
            //   if(odd==k)
            //  rightflag=right-1;//找到第k的那个奇数

            while (right < len&&nums[right]%2==0) {

                right++;
            }

            //此时跳出来的right是k+1
            int rightdistance = right - rightflag;//右边的偶数个数
            System.out.println("right"+right+" "+"rightdistance"+rightdistance);

            int leftdistance = 0;
            int leftflag = left;
            while (left < len && nums[left] % 2 == 0) {
                left++;
            }
            System.out.println("left的第一个循环结束后left的值"+left);

            //此时left在遇到的第一个奇数上
            if (left < len) {
                leftdistance = left - leftflag ;//闭区间
            }
            System.out.println("leftdistance"+leftdistance);
            res += (leftdistance+1) * (rightdistance + 1);
            left++;
            if (odd > 0)
                odd--;


        }
        return res;
    }
}
