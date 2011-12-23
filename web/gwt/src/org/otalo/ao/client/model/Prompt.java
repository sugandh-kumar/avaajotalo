/*
 * Copyright (c) 2009 Regents of the University of California, Stanford University, and others
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.otalo.ao.client.model;

public class Prompt extends BaseModel {
	
	public Prompt(JSOModel data) {
		super(data);
	}
	
	public String getName()
	{
		return getField("name");
	}

	public String getOrder()
	{
		return getField("order");
	}
	
	public Survey getSurvey()
	{
		return new Survey(getObject("fields").getObject("survey"));
	}
}