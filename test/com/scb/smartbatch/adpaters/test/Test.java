package com.mcg.batch.adpaters.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


class Test
{
	/*public static void main(String [] args) 
	{
		Test p = new Test();
		p.start();

		  Scanner in = new Scanner(System.in);
	        int output = 0;
	        int ip1 = Integer.parseInt(in.nextLine().trim());
	        int ip2 = Integer.parseInt(in.nextLine().trim());
	        int ip3_size = 0;
	        ip3_size = Integer.parseInt(in.nextLine().trim());
	        int[] ip3 = new int[ip3_size];
	        int ip3_item;
	        for(int ip3_i = 0; ip3_i < ip3_size; ip3_i++) {
	            ip3_item = Integer.parseInt(in.nextLine().trim());
	            ip3[ip3_i] = ip3_item;
	        }
	        output = GetJumpCount(ip1,ip2,ip3);
	        System.out.println(String.valueOf(output));
	}

		void start() 
	{
		String s1 = "slip";
		String s2 = fix(s1);
		System.out.println(s1 + " " + s2);
		int a=7;
		a=(4>>2);
		System.out.println(a/16f);
	}

	String fix(String s1) 
	{
		s1 = s1 + "stream";
		System.out.print(s1 + " ");
		return "stream";
	}

	public static int GetJumpCount(int input1,int input2,int[] input3)
	{
		if(null!=input3){
			int numOfjumps=0;
			int numOfWalls=input3.length;
			for(int curWall=0; curWall<numOfWalls;curWall++) {
				if((input1-input2)>=input3[curWall]){
					numOfjumps++;
					continue;
				}
				else{
					double d= (double)input3[curWall]/(double)(input1-input2);
					Double jumpsInDouble=Math.ceil(d);
					int numberOfJumpsRequired= jumpsInDouble.intValue();
					if((((numberOfJumpsRequired-2)*(input1-input2))+input1)>=input3[curWall] && jumpsInDouble>d){
						numberOfJumpsRequired=numberOfJumpsRequired-1;
					}
					numOfjumps=numOfjumps+numberOfJumpsRequired;
				}
			}
			return numOfjumps;
		}
		return 0;
	}
	 */

	/*public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);
		int output = 0;
		int ip1_rows = 0;
		int ip1_cols = 0;
		ip1_rows = Integer.parseInt(in.nextLine().trim());
		ip1_cols = Integer.parseInt(in.nextLine().trim());

		int[][] ip1 = new int[ip1_rows][ip1_cols];
		for(int ip1_i=0; ip1_i<ip1_rows; ip1_i++) {
			for(int ip1_j=0; ip1_j<ip1_cols; ip1_j++) {
				ip1[ip1_i][ip1_j] = in.nextInt();

			}
		}
		output = SolveMagicSquare(ip1);
		System.out.println(String.valueOf(output));
	}*/

	public static int SolveMagicSquare(int[][] input1)
	{



		int n=input1[0].length;
		int expectedSum=n*((n^2+1)/2);


		if(n>1 || n<20){
			if(input1[0][0]!=1){
				return 0;
			}


			if(validateSquareSum(input1,expectedSum)){

				validateSmallerSquares(input1, n);
			}
		}
		return 0;
	}



	static boolean validateSquareSum(int[][] square, int expectedSum) {



		int sum;

		// check row
		for(int i = 0; i < square.length; i++) {
			sum = 0;
			for(int j = 0; j < square[i].length; j++)
				sum += square[i][j];
			if(sum != expectedSum)
				return false;
		}

		// check column
		for(int i = 0; i < square.length; i++) {
			sum = 0;
			for(int j = 0; j < square[i].length; j++)
				sum += square[j][i];
			if(sum != expectedSum)
				return false;
		}

		/*   // check first diag
		   sum = 0;
		   for(int i = 0; i < square.length; i++)
		     sum += square[i][i];
		   if(sum != expectedSum)
		     return false;

		   // check second diag
		   sum = 0;
		   for(int i = square.length -1; i >= 0; i--)
		      sum += square[i][i];
		   if(sum != expectedSum)
		     return false;
		 */


		return true;

	}

	private static void validateSmallerSquares(int[][] square,int N) {
		for(int i=1;i<=N;i++){
			int numOfElemPerRowInInnerSquare=(int)Math.sqrt(N);
			int startIndexColumnForCompare=(i)+numOfElemPerRowInInnerSquare;
			for(int j=1;j<=N;j++){
				/*	int 
				for(int j=(i+1)*numOfElemPerRowInInnerSquare;j<)
				 */
			}

		}
	}

	/*	private static int[][] getSmallerSquaresInterChanged(int[][] matrix, int subSquareIndexSource){
			int[][] swappedMatrix=new int[matrix.length][matrix[0].length];
			for(int i=0; i<matrix.length; i++)
				for(int j=0; j<matrix[i].length; j++)
					swappedMatrix[i][j]=matrix[i][j];

			int numberOfSquarePerRowColumn=(int)Math.sqrt(swappedMatrix.length);
			int targetSubsquareHorizontal=(subSquareIndexSource)%numberOfSquarePerRowColumn;
			for(int z=1; z<){
			swapArrayElementsHorizontal(swappedMatrix,subSquareIndexSource, i);
			}
			return swappedMatrix;
		}

		private static void swapArrayElementsHorizontal(int[][] swappedMatrix, int subSquareIndexSource, int subSquareIndexTarget) {
			int numberOfSquarePerRowColumn=(int)Math.sqrt(swappedMatrix.length);
			for(int i=0; i<swappedMatrix.length; i++){
				int subSquareXIndex=(i+1)%numberOfSquarePerRowColumn;

				for(int j=0; j<swappedMatrix[i].length; j++){
					int subSquareYIndex=(j+1)%numberOfSquarePerRowColumn;

					int original=swappedMatrix[i][j];
					int target=swappedMatrix[subSquareXIndex*numberOfSquarePerRowColumn]
				}
			}

		}*/


	public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);

		int output = 0;
		int ip1_cols = 0;
		ip1_cols = Integer.parseInt(in.nextLine().trim());

		in.useDelimiter(" ");
		char[] ip1 = new char[ip1_cols];
		for(int ip1_j=0; ip1_j<ip1_cols; ip1_j++) {
			ip1[ip1_j] = in.next().charAt(0);

		}
		enumerate(ip1);
		for (Iterator iterator = possibleAleternatives.iterator(); iterator.hasNext();) {
			String cs = (String) iterator.next();
			System.out.println(cs);
		}

	}
	static Set<String> possibleAleternatives=new HashSet();

	static void enumerate(char[] charArr){

		for (int i = 0; i < charArr.length; i++) {
			char c = charArr[i];

			if(c=='?'){
				findPossibleAlternatives(charArr, i);
			}
		}
		String s=String.valueOf(charArr);
		if(!s.contains("?")){
		possibleAleternatives.add(s);
		}
	}

	static void findPossibleAlternatives(char[] charArray,int i ){
		char[] newArr1=Arrays.copyOf(charArray, charArray.length);
		char[] newArr=Arrays.copyOf(newArr1, newArr1.length);
		newArr1[i]='0';
		enumerate(newArr1);
		newArr1=null;
		newArr[i]='1';
		enumerate(newArr);
		newArr=null;


	}
}