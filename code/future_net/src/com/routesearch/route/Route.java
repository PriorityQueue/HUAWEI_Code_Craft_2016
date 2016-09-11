/**
 * 实现代码文件
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;
public final class Route
{
    /**
     * 你需要完成功能的入口
     * 
     * @author XXX
     * @since 2016-3-4
     * @version V1
     * @throws CloneNotSupportedException 
     */
	static int number;
	static final int LIMIT = 310;
	static final int SIZE = 100;
	static final int K=3;
	private static final long start = System.currentTimeMillis();
	static int LIMIT_TIME =9500;
	public static String searchRoute(String graphContent, String condition) throws CloneNotSupportedException
    {
		
    	/*记录起止点与必经节点*/
    	String firstLastVer = condition.substring(0, condition.lastIndexOf(','));
    	String first = firstLastVer.substring(0, firstLastVer.indexOf(','));
    	String last  = firstLastVer.substring(firstLastVer.indexOf(',')+1,firstLastVer.length());
    	Short First = new Short(first);
    	Short Last  = new Short(last);
    	
		/*建图*/
		MyGraph g = buildGraph(graphContent,first);
		
		number=g.vertices.size();
		
    	//去掉终点的所有出度边
    	g.vertices.put(Last, null);
    	
    	String keyVertex  = condition.substring(condition.lastIndexOf(',')+1,condition.length());
    	
    	/*去掉结尾的换行符*/
    	String[] keyVer=keyVertex.split("\n");
    	
    	/*按‘|’拆分必经节点*/
    	String[] keyV = keyVer[0].split("\\|");
    	
    	/*将必经节点存放在map中*/
    	Map<Short,Boolean> keyVertexMap =new HashMap<Short,Boolean>();
    	for(String s:keyV)
    		keyVertexMap.put(new Short(s), true);

    	
    	/*计算起点到各必经节点最短路径*/
    	Map<Short,DijResult> shortestPath = new HashMap<Short,DijResult>();
    	DijResult r=g.getShortestPath(First, keyVertexMap);
    	shortestPath.put(First, r);

    	//去掉起点构建新的必经点集合
    	keyVertexMap.remove(First);
    	
    	/*计算各必经节点与最终节点之间的最短路径*/  	
    	keyVertexMap.put(Last, true);
    	Short keyId;
    	for(String s:keyV){
			keyId = new Short(s);
			DijResult tempR = g.getShortestPath(keyId,keyVertexMap);
    		shortestPath.put(keyId, tempR);
    	}   
    	keyVertexMap.remove(Last);
    	return SK(g,First,Last,shortestPath,keyVertexMap);
    }

	private static MyGraph buildGraph(String graphContent ,String First){
		MyGraph g = new MyGraph();
    	String graphArr[] = graphContent.split("\n");
    	String[] arcArr = new String[4];
    	for(String s:graphArr){
    		arcArr=s.split(",");
			/* ？？？？？包装类的创建会不会拖慢速度？？？？？？？ */
			if (!arcArr[2].equals(First))
				/*若该边不是指向起点则添加到图里*/
				g.addVertex(new Short(arcArr[1]), 
						new MyVertex(new Short(arcArr[2]), new Integer(arcArr[3]),new Integer(arcArr[0])));
		}
    	return g;
	}
	public static String SK(MyGraph g,Short first,Short last,Map<Short,DijResult> shortestPath,Map<Short,Boolean> keyVertexMap){
		Map<Short,PriorityQueue<Map<Short, MyVertex>>> ff = new HashMap<Short,PriorityQueue<Map<Short, MyVertex>>>();
		Map<Short,PriorityQueue<Map<Short, MyVertex>>> result = new HashMap<Short,PriorityQueue<Map<Short, MyVertex>>>();
		Map<Pair<Short,Short>,ArrayList<Map<Short, MyVertex>>> allTheKSP =new HashMap<Pair<Short,Short>,ArrayList<Map<Short, MyVertex>>>();
		for(Short vi :keyVertexMap.keySet()){
			for(Short vl :keyVertexMap.keySet()){
				if(vi.equals(vl))
					continue;
				else{
					allTheKSP.put(new Pair<Short, Short>(vi,vl),yen(vi, vl, K, g));
				}
			}
		}
		for(Short vi :keyVertexMap.keySet()){
			allTheKSP.put(new Pair<Short, Short>(vi,last),yen(vi, last, K, g));
			allTheKSP.put(new Pair<Short, Short>(first,vi),yen(first, vi, K, g));
		}
		for (Short keyVer : keyVertexMap.keySet()) {
			ArrayList<Map<Short, MyVertex>> kp = allTheKSP.get(new Pair<Short,Short>(keyVer,last));//)yen(keyVer, last, K, g);
			PriorityQueue<Map<Short, MyVertex>> tempArrMap = new PriorityQueue<Map<Short, MyVertex>>(K,
					new PathComparator());
			for (Map<Short, MyVertex> preMap : kp) {
				int count = 0;// 计算该路径包含关键点个数
				for (Short s : preMap.keySet())
					if (keyVertexMap.get(s) != null)
						count++;
				preMap.put(new Short("-1"), new MyVertex(new Short("-1"), count));
				tempArrMap.add(preMap);
			}
			ff.put(new Short(keyVer), tempArrMap);
		}
		
		iterator: 
		for (int i = 1; i < keyVertexMap.size(); ++i) {
			Map<Short, PriorityQueue<Map<Short, MyVertex>>> tempff = new HashMap<Short, PriorityQueue<Map<Short, MyVertex>>>();
			for (Short vi : keyVertexMap.keySet()) {
				PriorityQueue<Map<Short, MyVertex>> tempArry = new PriorityQueue<Map<Short, MyVertex>>(
						SIZE * K * number, new PathComparator());
				second: 
				for (Short Vl : keyVertexMap.keySet()) {
					PriorityQueue<Map<Short, MyVertex>> tempViToVl = new PriorityQueue<Map<Short, MyVertex>>(
							SIZE * K , new PathComparator());
					long current = System.currentTimeMillis();
					if((current - start)>LIMIT_TIME)
						break iterator;
					/* 求 vi(vi) 到 vl 的路径 */
					if (Vl.equals(vi) || ff.get(Vl) == null || ff.get(Vl).size() <= 0 ) {
						/* 若不存在从Vl到last的f路径 || vi==vl 则跳过这个Vl */
						continue;
					}
					ArrayList<Map<Short, MyVertex>> kp = allTheKSP.get(new Pair<Short,Short>(vi,Vl));	
					
					if (kp==null||kp.size()<=0) {
						/* 若不存在从Vl到last的f路径 || vi==vl 则跳过这个Vl */
						continue;
					}
						PriorityQueue<Map<Short, MyVertex>> tempQ = new PriorityQueue<Map<Short, MyVertex>>(
								ff.get(Vl));
						VlMark: 
						for (int k = 0; k < SIZE; ++k) {
							if (tempQ.isEmpty())//tempQ里的路径数不足SIZE个
								break VlMark;
							Map<Short, MyVertex> minPath = tempQ.remove();
							if(minPath.get(vi)!=null)
								continue VlMark;
							
							kspMark: 
							for (Map<Short, MyVertex> ViToVl : kp) {
								if(ViToVl.size()<=2)
									continue kspMark;
							Map<Short, MyVertex> fpath = new HashMap<Short, MyVertex>(minPath);
							int count = 0;
							Short pre =Vl;
							MyVertex vPoint = ViToVl.get(pre);
							/*
							 * 将vi到vl的路径与vl到last的路径连连接，生成一条vi到last的新路径并保存在fpath中
							 */
							while (vPoint != null) {
								if (fpath.get(vPoint.getId()) != null) {
									/* vi到vl路径中的点 出现在vl到last的路径中 不再生成此路径 */
									continue kspMark;
								}
								if (keyVertexMap.get(vPoint.getId()) != null)
									count++;
								fpath.put(pre, vPoint);
								pre = vPoint.getId();
								vPoint = ViToVl.get(pre);
							}
							
							fpath.put(pre, vPoint);
							
							if (!pre.equals(vi)){
								System.out.println("====ERROR=====");
							}
							count += fpath.get(new Short("-1")).getDistance();
							fpath.put(new Short("-1"), new MyVertex(new Short("-1"), count));

							int tempCost = countPathCost(fpath, last, g);
							fpath.put(new Short("-2"), new MyVertex(new Short("-2"), tempCost));
							if (count == keyVertexMap.size()) {
								PriorityQueue<Map<Short, MyVertex>> tempAL;
								if (result.get(vi) == null)
									tempAL = new PriorityQueue<Map<Short, MyVertex>>(SIZE * K * number,
											new PathComparator());
								else
									tempAL = result.get(vi);
								tempAL.add(fpath);
								result.put(vi, tempAL);
							} else if (count < keyVertexMap.size()) {
								tempArry.add(fpath);
								tempViToVl.add(fpath);
							} else if (count > keyVertexMap.size()) {
								System.out.println("====ERROR 256=====");
								break iterator;
							}
							break kspMark;
							}//ksp for
						} 
						// for
						if(!tempViToVl.isEmpty()){
						Map<Short, MyVertex> best =tempViToVl.peek();
						int bestCost = best.get(new Short("-2")).getDistance();
						int bestCount = best.get(new Short("-1")).getDistance();
						PriorityQueue<Map<Short, MyVertex>> beforIter =ff.get(vi);
						for(Map<Short, MyVertex> past:beforIter){
							if(past.get(new Short("-2")).getDistance()>bestCost)
								break;
							else
								if(past.get(Vl)!=null && past.get(new Short("-1")).getDistance()>=bestCount)
									tempViToVl.add(past);
						}
						}
						//tempArry.addAll(tempViToVl);
						
				}
				//System.out.println(tempArry.size());
				
				tempff.put(new Short(vi), tempArry);
			}
			ff.clear();
			//System.out.println("==========");
			ff = tempff;
		}
	
		/*======将first到vi的路径与vi到last的路径连接=========*/
		for(Short vi:result.keySet()){
		/*将迭代过程中已经包含所有关键节点的vi到last的路径并入迭代完成后生成的vi到last路径中去*/
			if(ff.get(vi) == null)
				ff.put(vi, result.get(vi));
			else
				ff.get(vi).addAll(result.get(vi));
		}
		
		PriorityQueue<Map<Short, MyVertex>> tempArr = new PriorityQueue<Map<Short, MyVertex>>(
				SIZE * K * ff.keySet().size(), new PathComparator()); // 保存一条fist到last的路径
		for (Short vi : ff.keySet()) {
			if (ff.get(vi) == null || ff.get(vi).size() <= 0)
				/* 若不存在从Vi到last的f路径 */
				continue;
			ArrayList<Map<Short, MyVertex>> kp = allTheKSP.get(new Pair<Short, Short>(first, vi)); // yen(first,
																									// vi,
																									// K,
																									// g);
			if (kp.size() <= 0)
				/* 不存在first到vi的路径 则跳过这个Vi */
				continue;

			// for(Map<Short, MyVertex> firstToVi:kp){
			/* 获取first到vi(v)的路径 */
			viTolast: 
			for (Map<Short, MyVertex> tempfpath : ff.get(vi)) {
				if (tempfpath.get(new Short("-1")).getDistance() != keyVertexMap.size()) {
					// continue;
					/* vi 到last的路径中没有包含所有的关键节点 */
					System.out.println("====ERROR vi到last的路径中没有包含所有的关键节点====");
					continue;
				}
				kMark: 
				for (Map<Short, MyVertex> firstToVi : kp) {
					if(firstToVi.size()<=2)
						continue kMark;
					Map<Short, MyVertex> fpath = new HashMap<Short, MyVertex>(tempfpath);
					Short pre = vi;
					MyVertex vPoint = firstToVi.get(pre);
					/*将first到vi的路径与vi到last的路径连连接，生成一条first到last的新路径并保存在fpath中*/
					while (vPoint != null) {
						if (fpath.get(vPoint.getId()) != null) {
							/* first到vi路径中的点 出现在vi到last的路径中 不再生成此路径 */
							continue kMark;
						}
						/*
						 * if (vPoint!=null && keyVertexMap.get(vPoint.getId())
						 * != null) count++; //first 到 vi的路径中还包含了关键节点
						 */ 
						fpath.put(pre, vPoint);
						pre = vPoint.getId();
						vPoint = firstToVi.get(pre);
					}
					fpath.put(pre, vPoint);
					if (!pre.equals(first)) {
						System.out.println("====ERROR 289=====");
					}
					int tempCost = countPathCost(fpath, last, g);
					fpath.put(new Short("-2"), new MyVertex(new Short("-2"), tempCost));
					tempArr.add(new HashMap<Short, MyVertex>(fpath));
					break kMark;
				}
			}
			// }//ksp
		}
		
		
		/* =====计算所有合法路径中权值最小的路径======== */

		Integer min = Integer.MAX_VALUE;

		Map<Short, MyVertex> path;
		do {
			if (tempArr.isEmpty()) {
				System.out.println("NA");
				return "NA";
			}
			path = tempArr.remove();
		} while (path.get(new Short("-1")).getDistance() != keyVertexMap.size());
		
		
		int sumCost = 0;
		Short pre = last;
		MyVertex VPoint = path.get(pre);
		Stack<Integer> s = new Stack<Integer>();
		while (VPoint != null) {
			int minCost = Integer.MAX_VALUE;// VPoint to pre
											// 的最小代价（因为两点之间可能存在多条连接的边）
			Integer tempArcId = -1;
			for (MyVertex v : g.vertices.get(VPoint.getId())) {
				if (v.getId().equals(pre)) {
					if (v.getDistance() < minCost) {
						minCost = v.getDistance();
						tempArcId = v.getArcId();
					}
				}
			}
			sumCost += minCost;
			s.push(tempArcId);
			pre = VPoint.getId();
			VPoint = path.get(VPoint.getId());
		}
		if (!pre.equals(first))
			System.out.println("======= ERROR ========");

		StringBuffer sb = new StringBuffer();
		if (s.isEmpty()) {
			System.out.println("NA");
			return "NA";
		} else {
			// printPath(path,last);
			while (!s.isEmpty())
				sb.append(s.pop().toString() + "|");
			sb.deleteCharAt(sb.length() - 1);
			String finalResult = sb.toString();
			System.out.println(finalResult);
			System.out.println("cost:" + sumCost);
			return finalResult;
		}

	}

	public static Map<Short, MyVertex> listToMap (List<MyVertex> vertexList){
		Map<Short, MyVertex> mp = new HashMap<Short, MyVertex>();
		for(int i=vertexList.size()-1;i>0;--i){
			mp.put((short)vertexList.get(i).getId(), new MyVertex((short)vertexList.get(i-1).getId(),0));
		}
		mp.put((short)vertexList.get(0).getId(), null);
		return mp;
	}
	public static ArrayList<MyVertex> MapToList(Map<Short, MyVertex> p,Short last){
		ArrayList<MyVertex> l = new ArrayList<MyVertex>();
		Short pre =last;
		MyVertex vpoint = p.get(last);
		l.add(new MyVertex(last,0));
		while(vpoint!=null){
			l.add(0,new MyVertex(vpoint));
			pre=vpoint.getId();
			vpoint = p.get(pre);
		}
		return l;
	}

	public static ArrayList<Map<Short, MyVertex>> yen(Short start, Short last, int k, MyGraph g) {
		MyGraph mg = new MyGraph(g);//new 一个新图防止传入的图被修改
		ArrayList<Map<Short, MyVertex>> A = new ArrayList<Map<Short, MyVertex>>();//K短路径容器
		PriorityQueue<Map<Short, MyVertex>> B = 
				new PriorityQueue<Map<Short, MyVertex>>(k, new PathCostComparator());//临时存放K短路径
		Map<Short, MyVertex> R = new HashMap<Short, MyVertex>();//存放从原点到偏移点的路径
		
		//Map<Short, MyVertex> S = new HashMap<Short, MyVertex>();

		/*first：计算第一条最短路径*/
		Map<Short, Boolean> lastMap = new HashMap<Short, Boolean>();
		lastMap.put(last, true);
		DijResult shortestPath = mg.getShortestPath(start, lastMap);
		
		/*计算路径权值，并将该路径放入B中*/
		Map<Short, MyVertex> BPath = getThePath(shortestPath.getPrevious(),last);
		
		int cost = countPathCost(BPath, last, g);
		BPath.put(new Short("-2"), new MyVertex(new Short("-2"), cost));
		
		B.add(BPath);
		int count=0;
		/*开始计算路径权值*/
		done: 
		while (!B.isEmpty()) {
			/*B中有待出队的K短路径作为偏移路径则可继续获得K短路径*/
			//if (B.size() + A.size() >= k)
				/*如果已计算出的k短路径数满足要求则退出*/
				//break done;
			/*弹出第k短路径作为之后计算的偏移路径*/
			Map<Short, MyVertex> kShortestPath = B.remove();
			
			A.add(new HashMap<Short, MyVertex>(kShortestPath));
			count++;
			if(count>=k)
				break done;
			ArrayList<MyVertex> Path = MapToList(kShortestPath, last);
			for (int i = 0; i < Path.size() - 1; ++i) {
				/*从路径第一个点开始偏移*/
				MyVertex v = Path.get(i);
				if (i > 0)
					R.put(v.getId(), Path.get(i - 1));
				else
					R.put(v.getId(), null);
				
				/*移除该点的下一条在路径上的边*/
				ArrayList<MyVertex> next=mg.vertices.get(v.getId());
				for(MyVertex mv:next)
					if(mv.getId().equals(Path.get(i + 1).getId())){
						next.remove(mv);
						break;
					}
				/*计算从该点出发的第k短路径*/
				DijResult sPath = mg.getShortestPath(v.getId(), lastMap);
				Map<Short, MyVertex> tempSP =sPath.getPrevious();
				if(tempSP.size()==1||tempSP.get(last)==null)
					continue;
				BPath = getThePath(tempSP,last);
				
				ArrayList<MyVertex> Path2 = MapToList(BPath, last);
				
				/*删掉第k短路所采用的从该点出发的那条边*/
				for(MyVertex mv:next)
					if(mv.getId().equals(Path2.get(1).getId())){
						next.remove(mv);
						break;
					}
				/*连接*/
				if(BPath.size()<=0)
					continue;
				BPath.putAll(R);
				Path2 = MapToList(BPath, last);
				/*计算权值放入B中*/
				int tempCost = countPathCost(BPath, last, g);
				BPath.put(new Short("-2"), new MyVertex(new Short("-2"), tempCost));
				B.add(BPath);

			}
		}
		/*将B中的K短路径放入A中*/
		/*while (!B.isEmpty())
			A.add(B.remove());*/
		return A;

	}
	
	public static Map<Short, MyVertex> getThePath(Map<Short, MyVertex> p,Short last){
		Map<Short, MyVertex> theP =new HashMap<Short, MyVertex>();
		Short pre =last;
		MyVertex vpoint =p.get(pre);
		while(vpoint!=null){
			theP.put(pre, vpoint);
			pre=vpoint.getId();
			vpoint=p.get(pre);
		}
		theP.put(pre, vpoint);
		return theP;
	}
	
	public static void printPath(Map<Short, MyVertex> p,Short last){
		Short pre=last;
		MyVertex vPiont= p.get(pre);
		System.out.println();
		System.out.print(last+"->");
		while(vPiont!=null)
		{
			System.out.print(vPiont.getId()+"->");
			pre=vPiont.getId();
			vPiont= p.get(pre);
		}
		System.out.println();
	}
	public static int countPathCost(Map<Short, MyVertex> path,Short last,MyGraph g) {
		int sumCost = 0;
		Short pre = last;
		MyVertex VPoint = path.get(pre);
		while (VPoint != null) {
			int minCost = Integer.MAX_VALUE;// VPoint to pre
											// 的最小代价（因为两点之间可能存在多条连接的边）
			for (MyVertex v : g.vertices.get(VPoint.getId())) {
				if (v.getId().equals(pre)) {
					if (v.getDistance() < minCost)
						minCost = v.getDistance();
				}
			}
			//if(minCost == Integer.MAX_VALUE)
				
			sumCost += minCost;
			pre = VPoint.getId();
			VPoint = path.get(VPoint.getId());
		}
		return sumCost;
	}

}

class MyVertex implements Comparable<MyVertex>,Cloneable {
	
	private Short id;
	private Integer distance;
	private Integer arcId;
	
	public MyVertex(Short id, Integer distance){
		super();
		this.id = id;
		this.distance = distance;
	}
	public MyVertex(Short id, Integer distance,Integer arcId){
		super();
		this.id = id;
		this.distance = distance;
		this.arcId=arcId;
	}
	public MyVertex(MyVertex v){
		this.id = new Short(v.id);
		this.distance = new Integer(v.distance);
		//this.arcId = new Integer(v.arcId);
	}
	public Short getId() {
		return id;
	}

	public Integer getDistance() {
		return distance;
	}
	public Integer getArcId(){
		return arcId;
	}
	public void setId(Short id) {
		this.id = id;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}
	
	public void setArcId(Integer arcId){
		this.arcId=arcId;
	}
	
	@override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((distance == null) ? 0 : distance.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyVertex other = (MyVertex) obj;
		if (distance == null) {
			if (other.distance != null)
				return false;
		} else if (!distance.equals(other.distance))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Vertex [id=" + id + ", distance=" + distance + "]";
	}

	@Override
	public int compareTo(MyVertex o) {
		return this.distance < o.distance ? -1 : this.distance == o.distance ? 0 : 1;  
	}
	public MyVertex clone() throws CloneNotSupportedException{
		MyVertex cloned= (MyVertex) super.clone();
		cloned.distance = new Integer(this.distance);
		cloned.id 		= new Short(this.id);
		return cloned;
	}
	
}

class MyGraph implements Cloneable{
	
	public Map<Short, ArrayList<MyVertex>> vertices =new LinkedHashMap<Short, ArrayList<MyVertex>>(); ;
	
	public MyGraph() {
		this.vertices = new LinkedHashMap<Short, ArrayList<MyVertex>>(); 
	}
	public MyGraph(final MyGraph g){
		for(Short v:g.vertices.keySet()){
			if(g.vertices.get(v)!=null){
				ArrayList<MyVertex> l = new ArrayList<MyVertex>(g.vertices.get(v));
				this.vertices.put(v, l);
			}else{
				this.vertices.put(v, null);
			}
		}
		
	}
	public void addVertex(Short Short, MyVertex vertex) {
		if(this.vertices.get(Short)!=null)
			/*若图中已存在short则直接添加此出度边*/
			this.vertices.get(Short).add(vertex);
		else{
			/*若不存在则创建此顶点，并添加此边*/
			ArrayList<MyVertex> arc = new ArrayList<MyVertex>(8);
			arc.add(vertex);
			this.vertices.put(Short, arc);
		}
	}
	
	public ArrayList<MyVertex> getNeighbor (Short id){
		return vertices.get(id);
	}
	
	public void print(){
		for(Short key:vertices.keySet())
			for(MyVertex v:vertices.get(key))
				System.out.println(key.toString()+","+v.getId().toString()+","+v.getDistance().toString());
	}
	public DijResult getShortestPath(Short start,Map<Short,Boolean> V ){
		Map<Short, Integer> distances = new LinkedHashMap<Short, Integer>();
		PriorityQueue<MyVertex> nodes = new PriorityQueue<MyVertex>();
		Map<Short, MyVertex> previous = new LinkedHashMap<Short, MyVertex>();
		Map<Short,Boolean> set = new HashMap<Short,Boolean>(); //++
		
		//int Vcounter = V.size();
		for(Short vertex : vertices.keySet()) {
			if (vertex .equals(start)) {
				distances.put(vertex, 0);
				nodes.add(new MyVertex(vertex, 0));
				previous.put(vertex, null); // 1 ++
			} else {
				distances.put(vertex, Integer.MAX_VALUE);
			}
		}
		//System.out.println(" "+V.size());
		
		/*连通路径和上的点已遍历完，或是所有必经节点都已找到连接的最短路径*/
		int count = V.size();
		while (!nodes.isEmpty() && count>0) { 
			MyVertex smallest = nodes.poll();
			if(V.get(smallest.getId()) !=null){
				/*找到一个必经节点的最短路径*/
				--count;
			}
			
			/*将该点并入最短路径中*/
			set.put(smallest.getId(),true); 

			if (distances.get(smallest.getId()) == Integer.MAX_VALUE) {
				break;
			}
			if(vertices.get(smallest.getId())!=null)			
				for (MyVertex neighbor : vertices.get(smallest.getId())) {

/*					if (set.get(neighbor.getId()) != null) {
						 该点已被并入最短路径中 
						continue;
					}*/
					Integer alt = distances.get(smallest.getId()) + neighbor.getDistance();
					if(distances.get(neighbor.getId())==null)
						System.out.println();
					if (alt < distances.get(neighbor.getId()) && distances.get(neighbor.getId()) == Integer.MAX_VALUE) {
						distances.put(neighbor.getId(), alt);
						previous.put(neighbor.getId(), smallest);
						nodes.add(new MyVertex(neighbor.getId(), alt));// 2 ++
					} else if (alt < distances.get(neighbor.getId())) {
						previous.put(neighbor.getId(), smallest);
						distances.put(neighbor.getId(), alt);
						forloop: // 3 ++
						for (MyVertex n : nodes) {
							if (n.getId() == neighbor.getId()) {
								n.setDistance(alt);
								break forloop;
							}
						}
					}
				}
		}
		
		return new DijResult(distances, previous);
	}

}

class PathComparator implements Comparator<Map<Short,MyVertex>>{
	@Override
	public int compare(Map<Short, MyVertex> o1, Map<Short, MyVertex> o2) {
		// TODO Auto-generated method stub
/*		Short countLocat = new Short("-1");
		Integer countO1 =o1.get(countLocat).getDistance();
		Integer countO2 =o2.get(countLocat).getDistance();
		Short costLocat = new Short("-2");
		Integer costO1 =o1.get(costLocat).getDistance();
		Integer costO2 =o2.get(costLocat).getDistance();
		if(countO1.compareTo(countO2)==0)
			return costO1.compareTo(costO2);
		else
			return countO1.compareTo(countO2)*-1;*/
		Short costLocat = new Short("-2");
		Integer costO1 =o1.get(costLocat).getDistance();
		Integer costO2 =o2.get(costLocat).getDistance();
		return costO1.compareTo(costO2);
	}
}
class PathCostComparator implements Comparator<Map<Short,MyVertex>>{
	@Override
	public int compare(Map<Short, MyVertex> o1, Map<Short, MyVertex> o2) {
		// TODO Auto-generated method stub
		Short costLocat = new Short("-2");
		Integer costO1 =o1.get(costLocat).getDistance();
		Integer costO2 =o2.get(costLocat).getDistance();
		return costO1.compareTo(costO2);


	}
}
class VertexComparator implements Comparator<Map<Short,MyVertex>>{
	@Override
	public int compare(Map<Short, MyVertex> o1, Map<Short, MyVertex> o2) {
		// TODO Auto-generated method stub
		Short costLocat = new Short("-2");
		Integer costO1 =o1.get(costLocat).getDistance();
		Integer costO2 =o2.get(costLocat).getDistance();
		return costO1.compareTo(costO2);
	}
}

class DijResult implements Cloneable{
	private Map<Short, Integer> distances;
	private Map<Short, MyVertex> previous;
	public DijResult(Map<Short, Integer> distances,Map<Short, MyVertex> previous){
		this.distances=distances;
		this.previous=previous;
	}
	public Map<Short, Integer> getDistances() {
		return distances;
	}
	
	public Map<Short, MyVertex> getPrevious() {
		return previous;
	}
	public DijResult clone() throws CloneNotSupportedException{
		DijResult cloned =(DijResult)super.clone();
		cloned.previous = new HashMap<Short, MyVertex>(this.previous);
		cloned.distances = new HashMap<Short, Integer>(this.distances);
		return cloned;
	}
	public DijResult(){
		super();
	}
	public DijResult(DijResult d){
		this.distances=new HashMap<Short, Integer>(d.distances);
		this.previous=new HashMap<Short, MyVertex>(d.previous);
	}
}