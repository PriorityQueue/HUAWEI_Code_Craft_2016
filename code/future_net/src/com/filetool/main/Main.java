package com.filetool.main;

import java.util.Scanner;

//import java.util.Scanner;

import com.filetool.util.FileUtil;
import com.filetool.util.LogUtil;
import com.routesearch.route.Route;



/**
 * 工具入口
 * 
 * @author
 * @since 2016-3-1
 * @version v1.0
 */
public class Main
{
    public static void main(String[] args) throws CloneNotSupportedException
    {
    	
/*        if (args.length != 3)
        {
        	//System.err.println(args.length);
            System.err.println("please input args: graphFilePath, conditionFilePath, resultFilePath");
            return;
        }*/


//        String graphFilePath =args[0];
//        String conditionFilePath = args[1];
//        String resultFilePath = args[2];
		String graphFilePath = null, conditionFilePath=null, resultFilePath=null;
		int no = 0;
		Scanner in = new Scanner(System.in);
		System.out.println("请输入样例编号：");
		no = in.nextInt();
		switch (no) {
		case 22:
			graphFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case22\\topo.csv";
			conditionFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case22\\demand.csv";
			resultFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case22\\result.csv";
			break;
		case 1:
			graphFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case1\\topo.csv";
			conditionFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case1\\demand.csv";
			resultFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case1\\result.csv";
			break;
		case 2:
			graphFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case2\\topo.csv";
			conditionFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case2\\demand.csv";
			resultFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case2\\result.csv";
			break;
		case 3:
			graphFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case3\\topo.csv";
			conditionFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case3\\demand.csv";
			resultFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case3\\result.csv";
			break;
		case 4:
			graphFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case4\\topo.csv";
			conditionFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case4\\demand.csv";
			resultFilePath = "C:\\Users\\Admin\\Desktop\\test-case\\case4\\result.csv";
			break;
		}
        
        LogUtil.printLog("Begin");

        // 读取输入文件
        String graphContent = FileUtil.read(graphFilePath, null);
        String conditionContent = FileUtil.read(conditionFilePath, null);
        
        
        // 功能实现入口
       String resultStr = Route.searchRoute(graphContent, conditionContent);
        // 写入输出文件
       FileUtil.write(resultFilePath, resultStr, false);

       LogUtil.printLog("End");
    }



}
