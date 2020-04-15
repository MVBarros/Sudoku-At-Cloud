package metrics.tools;//
// StatisticsBranch.java
//
// This program measures and instruments to obtain different statistics
// about Java programs.
//
// Copyright (c) 1998 by Han B. Lee (hanlee@cs.colorado.edu).
// ALL RIGHTS RESERVED.
//
// Permission to use, copy, modify, and distribute this software and its
// documentation for non-commercial purposes is hereby granted provided 
// that this copyright notice appears in all copies.
// 
// This software is provided "as is".  The licensor makes no warrenties, either
// expressed or implied, about its correctness or performance.  The licensor
// shall not be liable for any damages suffered as a result of using
// and modifying this software.
import org.json.JSONObject;

public class StatisticsBranch 
{
	String class_name_;
	String method_name_;
	int pc_;
	int taken_;
	int not_taken_;

	public StatisticsBranch(String class_name, String method_name, int pc) 
		{
			class_name_ = class_name;
			method_name_ = method_name;
			pc_ = pc;
			taken_ = 0;
			not_taken_ = 0;
		}

	public void print() 
		{
			System.out.println(class_name_ + '\t' + method_name_ + '\t' + pc_ + '\t' + taken_ + '\t' + not_taken_);
		}
	
	public void incrTaken()
		{
			taken_++;
		}

	public void incrNotTaken() 
		{
			not_taken_++;
		}

	public String getClass_name_() {
		return class_name_;
	}

	public String getMethod_name_() {
		return method_name_;
	}

	public int getPc_() {
		return pc_;
	}

	public int getTaken_() {
		return taken_;
	}

	public int getNot_taken_() {
		return not_taken_;
	}
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("Class Name", getClass_name_());
		obj.put("Method Name", getMethod_name_());
		obj.put("Program Counter", getPc_());
		obj.put("Taken", getTaken_());
		obj.put("Not Taken", getNot_taken_());
		return obj;
	}

}

