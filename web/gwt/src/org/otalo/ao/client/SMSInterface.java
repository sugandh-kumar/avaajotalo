package org.otalo.ao.client;

import java.util.Date;
import java.util.List;

import org.otalo.ao.client.JSONRequest.AoAPI;
import org.otalo.ao.client.JSONRequest.AoAPI.ValidationError;
import org.otalo.ao.client.SMSList.SMSListType;
import org.otalo.ao.client.model.Forum;
import org.otalo.ao.client.model.JSOModel;
import org.otalo.ao.client.model.Line;
import org.otalo.ao.client.model.MessageForum;
import org.otalo.ao.client.model.Tag;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedStackPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.datepicker.client.DateBox;

public class SMSInterface extends Composite {
	private FormPanel smsForm;
	private DecoratedStackPanel stackPanel = new DecoratedStackPanel();
	private VerticalPanel outer, who, what, when;
	private HorizontalPanel controls = new HorizontalPanel();
	private HorizontalPanel datePanel;
	private Button sendButton;
	private Hidden dateField;
	private TextArea txtArea;
	private ListBox tags, groups;
	private Hidden lineid;
	private CheckBox numbers, usersByTag;
	private Label remainingCharsLabel, dateLabel;
	private final int SMS_MAX_LENGTH = 160;
	private DateBox dateBox;
	private RadioButton now, date;
	private ListBox hour, min;

	private final DateTimeFormat dateFormat = DateTimeFormat.getFormat("MMM-dd-yyyy"); //on server side string date should be converted back to date in this format only
	
	public interface Images extends Fora.Images {
		ImageResource group();
		ImageResource messagesgroup();
		ImageResource calendar();
	}
	
	public SMSInterface(Images images) {
		outer = new VerticalPanel();
		outer.setSize("100%","100%");
		smsForm = new FormPanel();
		smsForm.setWidget(outer);
		smsForm.setMethod(FormPanel.METHOD_POST);
		smsForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		
		smsForm.addSubmitCompleteHandler(new SMSComplete());
		
		stackPanel.setSize("100%", "100%");
		who = new VerticalPanel();
		who.setSize("100%", "100%");
		what = new VerticalPanel();
		what.setSize("100%", "100%");
		when = new VerticalPanel();
		when.setSize("100%", "100%");
		
		VerticalPanel whoPanel = new VerticalPanel();
		whoPanel.setSize("100%", "100%");
		numbers = new CheckBox("Numbers:");
		numbers.setName("bynumbers");
		usersByTag = new CheckBox("Users by Tag:");
		usersByTag.setName("bytag");
		
		TextArea numbersArea = new TextArea();
		numbersArea.setName("numbersarea");
		numbersArea.setSize("350px", "100px");
		numbersArea.addFocusHandler(new FocusHandler() {
			public void onFocus(FocusEvent event) {
				numbers.setValue(true);
			}
		});
		tags = new ListBox(true);
		tags.setName("tag");
		tags.setVisibleItemCount(5);
		tags.addFocusHandler(new FocusHandler() {
			public void onFocus(FocusEvent event) {
				usersByTag.setValue(true);
			}
		});
		
		groups = new ListBox();
		groups.addItem("", "-1");
		groups.setName("group");
		
		dateLabel = new Label("Broadcast Time: ");
		
		now = new RadioButton("when","Now");
		now.setFormValue("now");
		now.setValue(true);

		date = new RadioButton("when","Date");
		date.setFormValue("date");
		dateField = new Hidden("date");
		// Date Box
		
		dateBox = new DateBox();
		dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
		dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {

			public void onValueChange(ValueChangeEvent<Date> event) {
				now.setValue(false);
				date.setValue(true);
				Date d = event.getValue();
				dateField.setValue(DateTimeFormat.getFormat("MMM-dd-yyyy").format(d));
			}
		});

		// Hour box
		hour = new ListBox();
		hour.setName("hour");
		for(int i=0; i < 24; i++)
		{
			String hourStr;
			if (i < 10)
				hourStr = "0"+String.valueOf(i);
			else
				hourStr = String.valueOf(i);

			hour.addItem(hourStr);
		}
		hour.addChangeHandler(new ChangeHandler() {

			public void onChange(ChangeEvent event) {
				now.setValue(false);
				date.setValue(true);

			}
		});

		// Minute box
		min = new ListBox();
		min.setName("min");
		for(int i=0; i < 60; i+=5)
		{
			String minStr;
			if (i < 10)
				minStr = "0"+String.valueOf(i);
			else
				minStr = String.valueOf(i);

			min.addItem(minStr);
		}
		min.addChangeHandler(new ChangeHandler() {

			public void onChange(ChangeEvent event) {
				now.setValue(false);
				date.setValue(true);

			}
		});

		if (Messages.get().canManage())
		{
			HorizontalPanel groupPanel = new HorizontalPanel();
			groupPanel.setSpacing(10);
			groupPanel.add(new Label("Group: "));
			groupPanel.add(groups);
			
			whoPanel.add(groupPanel);
		}
		else
		{
			HorizontalPanel numbersPanel = new HorizontalPanel();
			numbersPanel.setSpacing(10);
			numbersPanel.add(numbers);
			numbersPanel.add(numbersArea);
			
			HorizontalPanel tagPanel = new HorizontalPanel();
			tagPanel.setSpacing(10);
			tagPanel.add(usersByTag);
			tagPanel.add(tags);
			
			
			whoPanel.add(numbersPanel);
			whoPanel.add(tagPanel);
		}
		
		who.add(whoPanel);
		
		txtArea = new TextArea();
		txtArea.setName("txt");
		txtArea.setSize("350px", "100px");
		
		HorizontalPanel remainCharsPanel = new HorizontalPanel();
		remainCharsPanel.setSpacing(5);
		remainCharsPanel.add(new Label("You have"));
		remainingCharsLabel = new Label(Integer.toString(SMS_MAX_LENGTH));
		remainCharsPanel.add(remainingCharsLabel);
		remainCharsPanel.add(new Label("characters left."));
		
		txtArea.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				onTextAreaContentChanged(remainingCharsLabel);
			}
		});
  	
  	what.setSpacing(10);
  	what.add(txtArea);
  	what.add(remainCharsPanel);
  	
  	 HorizontalPanel nowPanel = new HorizontalPanel();
     //nowPanel.setHorizontalAlignment(HasAlignment.ALIGN_LEFT);
     nowPanel.setSpacing(4);
     nowPanel.add(now);
     
     HorizontalPanel datePicker = new HorizontalPanel();
     datePicker.setSpacing(4);
     datePicker.add(date);
     datePicker.add(dateBox);
     datePicker.add(hour);
     datePicker.add(new Label(":"));
     datePicker.add(min);
     
     dateField = new Hidden("date");
  	
  	when.add(nowPanel);
	when.add(datePicker);
	when.add(dateField);
		
		stackPanel.add(who, createHeaderHTML(images.group(), "Recipients"), true);
		stackPanel.add(what, createHeaderHTML(images.messagesgroup(), "Message"), true);
		stackPanel.add(when, createHeaderHTML(images.calendar(), "Schedule"), true);
		
		controls = new HorizontalPanel();
		
		sendButton = new Button("Send", new ClickHandler() {
      public void onClick(ClickEvent event) {
      	setClickedButton();
      	smsForm.setAction(JSONRequest.BASE_URL + AoAPI.SEND_SMS);
        smsForm.submit();
      }
    });
		
		controls.setSpacing(10);
		controls.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		controls.add(sendButton);
		
		outer.add(stackPanel);
		outer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		outer.add(controls);
		lineid = new Hidden("lineid");
		lineid.setValue(Messages.get().getLine().getId());
		outer.add(lineid);
		
		initWidget(smsForm);
		
		loadTags();
	}
	
	public void loadTags()
	{	
		// Somewhat lazy way to not keep reloading
		// Assumes the admin can see only a single line at
		// a time (unless they are superuser), and assumes
		// line has at least one item associated with it
	  // (sh be at least 2 items counting blank space)
		if (tags.getItemCount() < 2)
		{
			JSONRequest request = new JSONRequest();		
			request.doFetchURL(AoAPI.TAGS, new TagRequestor());
		}
	}
	
	 private class TagRequestor implements JSONRequester {
		 
		public void dataReceived(List<JSOModel> models) {
			Tag t;
			
			for (JSOModel model : models)
		  	{
					t = new Tag(model);
					tags.addItem(t.getTag(), t.getId());
		  	}

		}
	 }
	 
	 public void loadGroups(List<JSOModel> models)
	 {	
		 Line l;
		 Forum g;
		 groups.clear();
		 
		 for (JSOModel model : models)
		 {
			 	l = new Line(model);
				// ASSUME one group per line
				g = l.getForums().get(0);
				groups.addItem(g.getName(), g.getId());
		  }
		}
	 
	 private class SMSComplete implements SubmitCompleteHandler {
			
			public void onSubmitComplete(SubmitCompleteEvent event) {
				JSOModel model = JSONRequest.getModels(event.getResults()).get(0);
				if (model.get("model").equals("VALIDATION_ERROR"))
				{
					String msg = model.get("message");
					int type = Integer.valueOf(model.get("type"));
					ConfirmDialog dlg = new ConfirmDialog(msg);
					dlg.show();
					dlg.center();
					
					sendComplete();
				}
				else
				{
					ConfirmDialog sent = new ConfirmDialog("SMS Scheduled!");
					sent.show();
					sent.center();
					
					sendComplete();
					Messages.get().displaySMS(SMSListType.SENT, 0);
					
				}
				
				
				
		}
	 }

	 private void onTextAreaContentChanged(final Label remainingCharsLabel)
	 {
		 int counter = new Integer(txtArea.getText().length()).intValue();
	
		 int charsRemaining = SMS_MAX_LENGTH - counter;
		 remainingCharsLabel.setText("" + charsRemaining);
		 if (charsRemaining >= 0)
			 remainingCharsLabel.setStyleName("sms_under");
		 else
			 remainingCharsLabel.setStyleName("sms_over");
	 }
	 
	 public void reset()
	 {
		 smsForm.reset();
		 // need to be explicit since these
		 // are activated indirectly (kinda weird)
		 numbers.setValue(false);
		 usersByTag.setValue(false);
		 
		 remainingCharsLabel.setText(Integer.toString(SMS_MAX_LENGTH));
		 remainingCharsLabel.setStyleName("sms_under");
		 
		 stackPanel.showStack(0);
	 }

	private void setClickedButton()
	{
		sendButton.setEnabled(false);
	}
		
	private void sendComplete()
	{
		sendButton.setEnabled(true);
	}
	
	private String createHeaderHTML(ImageResource resource,
			String caption) {
		AbstractImagePrototype imageProto = AbstractImagePrototype.create(resource);
		String captionHTML = "<table class='caption' cellpadding='0' cellspacing='0'>"
				+ "<tr><td class='lcaption'>"
				+ imageProto.getHTML()
				+ "</td><td class='rcaption'><b style='white-space:nowrap'>"
				+ caption + "</b></td></tr></table>";
		return captionHTML;
	}
	

}
