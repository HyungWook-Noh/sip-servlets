/*
 * TeleStax, Open Source Cloud Communications.
 * Copyright 2012 and individual contributors by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.servlet.management.client.configuration;

import org.mobicents.servlet.management.client.router.Console;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.ComboBoxListenerAdapter;

public class ConfigurationPage extends Panel {
	private static Object[][] concurrencyControlModes = new Object[][]{  
		new Object[]{"None"},  
		new Object[]{"Transaction"},
		new Object[]{"SipSession"},
		new Object[]{"SipApplicationSession"}
	};  
	
	private static Object[][] congestionControlPolicies = new Object[][]{  
		new Object[]{"DropMessage"},  
		new Object[]{"ErrorResponse"}
	};
	
	private static Object[][] sglcModes= new Object[][]{
		new Object[]{"trace"},
		new Object[]{"debug"},  
		new Object[]{"default"},
		new Object[]{"production"}		
	};;
	
	ComboBox ccms;
	ComboBox ccps;
	ComboBox sglc;
	TextField queueSize;
	TextField memoryThreshold;
	TextField congestionControlCheckingInterval;
	TextField baseTimerInterval;
	TextField t2Interval;
	TextField t4Interval;
	TextField timerDInterval;
	TextField stopGracefully;
	
	private ComboBox makeCombo(Store store, String field, ComboBoxListenerAdapter listener, String defaultValue) {
		final ComboBox box;
		box = new ComboBox();  
		box.setForceSelection(true);  
		box.setStore(store);  
		box.setDisplayField(field);  
		box.setMode(ComboBox.LOCAL);  
		box.setTriggerAction(ComboBox.ALL);  
		box.setEmptyText("Select Value");  
		box.setValueField(field);
		box.setSelectOnFocus(true);  
		box.setEditable(false);
		box.setHideLabel(true);
		box.setWidth(160); 
		box.setHideTrigger(false);
		box.addListener(listener);
		box.setValue(defaultValue);
		return box;
	}
	
	private void addLabeledControl(String label, Widget component, Panel panel) {
		Panel regionLabel = new Panel();
		regionLabel.setPaddings(0, 0, 0, 1);
		regionLabel.setBorder(false);
		regionLabel.setHtml(label);
		panel.add(regionLabel);
		panel.add(component);
	}
	
	public ConfigurationPage() {
		final FormPanel formPanel = new FormPanel();  

//		formPanel.setTitle("Concurrency and Congestion Control");  

		formPanel.setWidth(900);  
		formPanel.setFrame(true); 
		formPanel.setLabelWidth(75);

		// Create queue size text box
		queueSize = new TextField();  
		queueSize.setAllowBlank(false); 
		queueSize.setHideLabel(true);
		addLabeledControl("SIP Mesage Queue Size:", queueSize, formPanel);
		// Create memory threshold size text box
		memoryThreshold = new TextField();  
		memoryThreshold.setAllowBlank(false); 
		memoryThreshold.setHideLabel(true);
		addLabeledControl("Memory Threshold:", memoryThreshold, formPanel);
		// Create congestion control checking interval size text box
		congestionControlCheckingInterval = new TextField();  
		congestionControlCheckingInterval.setAllowBlank(false); 
		congestionControlCheckingInterval.setHideLabel(true);
		addLabeledControl("Congestion Control Checking Interval:", congestionControlCheckingInterval, formPanel);
		
		// Create base timer interval text box
		baseTimerInterval = new TextField();  
		baseTimerInterval.setAllowBlank(false); 
		baseTimerInterval.setHideLabel(true);
		addLabeledControl("JAIN SIP Base Timer Interval:", baseTimerInterval, formPanel);
		
		t2Interval = new TextField();  
		t2Interval.setAllowBlank(false); 
		t2Interval.setHideLabel(true);
		addLabeledControl("JAIN SIP Timer T2 Interval:", t2Interval, formPanel);
		
		t4Interval = new TextField();  
		t4Interval.setAllowBlank(false); 
		t4Interval.setHideLabel(true);
		addLabeledControl("JAIN SIP Timer T4 Interval:", t4Interval, formPanel);
		
		timerDInterval = new TextField();  
		timerDInterval.setAllowBlank(false); 
		timerDInterval.setHideLabel(true);
		addLabeledControl("JAIN SIP Timer D Interval:", timerDInterval, formPanel);
		
		//Concurrency control modes selector
		final Store ccmsStore = new SimpleStore(new String[]{"ccms"}, concurrencyControlModes);  
		ccmsStore.load();  
		ccms = makeCombo(ccmsStore, "ccms", 
				new ComboBoxListenerAdapter() {  
			public void onSelect(ComboBox comboBox, com.gwtext.client.data.Record record, int index) {  
				System.out.println("Concurrency control::onSelect('" + record.getAsString("ccms") + "')");  
			}  
		}, (String)concurrencyControlModes[1][0]);
		addLabeledControl("Concurrency control mode:", ccms, formPanel);

		final Store ccpsStore = new SimpleStore(new String[]{"ccps"}, congestionControlPolicies);  
		ccpsStore.load();  
		ccps = makeCombo(ccpsStore, "ccps", 
				new ComboBoxListenerAdapter() {  
			public void onSelect(ComboBox comboBox, com.gwtext.client.data.Record record, int index) {  
				System.out.println("Congestion control::onSelect('" + record.getAsString("ccps") + "')");  
			}  
		}, (String)congestionControlPolicies[1][0]);
		addLabeledControl("Congestion control policy:", ccps, formPanel);
		
//		int indexLoggingMode = 0;
//		String[] profiles = ConfigurationService.Util.getSyncInstance().listLoggingProfiles();
//		String currentProfile = ConfigurationService.Util.getSyncInstance().getLoggingMode();
//		if(profiles.length > 0) {
//			sglcModes = new Object[profiles.length][1];
//			
//			int i = 0;
//			for (String profile : profiles) {
//				sglcModes[i] = new Object[] {profile};
//				if(profile.equalsIgnoreCase(currentProfile)) {
//					indexLoggingMode = i;
//				}
//				i++;
//			}
//		}
//		if(sglcModes != null) {
			final Store sglcStore = new SimpleStore(new String[]{"sglc"}, sglcModes);  
			sglcStore.load();  
			sglc = makeCombo(sglcStore, "sglc", 
					new ComboBoxListenerAdapter() {  
				public void onSelect(ComboBox comboBox, com.gwtext.client.data.Record record, int index) {  
					System.out.println("Logging Mode::onSelect('" + record.getAsString("sglc") + "')");  
				}  
			}, (String)sglcModes[2][0]);
			addLabeledControl("Logging Mode: ", sglc, formPanel);
//		}
		
		//Save button
		Button save = new Button("Apply", new ButtonListenerAdapter(){

			public void onClick(Button button, EventObject e) {
				ConfigurationService.Util.getInstance().setConcurrencyControlMode(
						ccms.getValue(), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set concurreny control mode.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				ConfigurationService.Util.getInstance().setCongestionControlPolicy(
						ccps.getValue(), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set congestion control policy.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				if(sglc != null) {
					ConfigurationService.Util.getInstance().setLoggingMode(
							sglc.getValue(), new AsyncCallback<Void>() {
	
								public void onFailure(Throwable caught) {
									Console.error("Error while trying to set Logging Mode.");
								}
	
								public void onSuccess(Void result) {
									result = result;
								}
								
						});
				}
				
				ConfigurationService.Util.getInstance().setQueueSize(
						Integer.parseInt(queueSize.getValueAsString()), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set SIP message queue size.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				ConfigurationService.Util.getInstance().setBaseTimerInterval(
						Integer.parseInt(baseTimerInterval.getValueAsString()), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set the Base Timer Interval.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				ConfigurationService.Util.getInstance().setT4Interval(
						Integer.parseInt(t4Interval.getValueAsString()), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set the SIP Timer T4 Interval.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				ConfigurationService.Util.getInstance().setT2Interval(
						Integer.parseInt(t2Interval.getValueAsString()), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set the SIP Timer T2 Interval.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				ConfigurationService.Util.getInstance().setTimerDInterval(
						Integer.parseInt(timerDInterval.getValueAsString()), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set the SIP Timer D Interval.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				ConfigurationService.Util.getInstance().setMemoryThreshold(
						Integer.parseInt(memoryThreshold.getValueAsString()), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set memory Threshold.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				ConfigurationService.Util.getInstance().setCongestionControlCheckingInterval(
						Long.parseLong(congestionControlCheckingInterval.getValueAsString()), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set congestion control checking interval.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
			}
			
		});
		
		formPanel.add(save);

		add(formPanel);
		
		DeferredCommand.addCommand(new Command() {

			public void execute() {
				ConfigurationService.Util.getInstance().getQueueSize(new AsyncCallback<Integer>() {

					public void onFailure(Throwable caught) {
						Console.error("Error while trying to get SIP message queue size.");
					}

					public void onSuccess(Integer result) {
						queueSize.setValue(result.toString());
					}
					
				});
				
				ConfigurationService.Util.getInstance().getBaseTimerInterval(new AsyncCallback<Integer>() {

					public void onFailure(Throwable caught) {
						Console.error("Error while trying to get the base timer interval.");
					}

					public void onSuccess(Integer result) {
						baseTimerInterval.setValue(result.toString());
					}
					
				});
				
				ConfigurationService.Util.getInstance().getT2Interval(new AsyncCallback<Integer>() {

					public void onFailure(Throwable caught) {
						Console.error("Error while trying to get the SIP Timer T2 interval.");
					}

					public void onSuccess(Integer result) {
						t2Interval.setValue(result.toString());
					}
					
				});
				
				ConfigurationService.Util.getInstance().getT4Interval(new AsyncCallback<Integer>() {

					public void onFailure(Throwable caught) {
						Console.error("Error while trying to get the SIP Timer T4 interval.");
					}

					public void onSuccess(Integer result) {
						t4Interval.setValue(result.toString());
					}
					
				});
				
				ConfigurationService.Util.getInstance().getTimerDInterval(new AsyncCallback<Integer>() {

					public void onFailure(Throwable caught) {
						Console.error("Error while trying to get the SIP Timer D interval.");
					}

					public void onSuccess(Integer result) {
						timerDInterval.setValue(result.toString());
					}
					
				});
				
				ConfigurationService.Util.getInstance().getConcurrencyControlMode(
						new AsyncCallback<String>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to get concurreny control mode.");
							}

							public void onSuccess(String result) {
								ccms.setValue(result.toString());
							}
							
						});
				
				ConfigurationService.Util.getInstance().getMemoryThreshold(new AsyncCallback<Integer>() {

					public void onFailure(Throwable caught) {
						Console.error("Error while trying to get memory threshold.");
					}

					public void onSuccess(Integer result) {
						memoryThreshold.setValue(result.toString());
					}
					
				});
				
				ConfigurationService.Util.getInstance().getCongestionControlPolicy(
						new AsyncCallback<String>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to get congestion control policy.");
							}

							public void onSuccess(String result) {
								ccps.setValue(result.toString());
							}
							
						});
				
				ConfigurationService.Util.getInstance().getCongestionControlCheckingInterval(
						new AsyncCallback<Long>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to get congestion control checking interval.");
							}

							public void onSuccess(Long result) {
								congestionControlCheckingInterval.setValue(result.toString());
							}
							
						});
				
					ConfigurationService.Util.getInstance().getLoggingMode(
							new AsyncCallback<String>() {
	
								public void onFailure(Throwable caught) {
									Console.error("Error while trying to get logging mode.");
								}
	
								public void onSuccess(String result) {
									if(result != null)
										sglc.setValue(result.toString());
								}
								
							});
			}
			
		});
	}

}
