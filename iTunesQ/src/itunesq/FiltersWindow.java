package itunesq;

import java.io.IOException;
import java.util.Iterator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Window;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Class that handles the display filters window. This window allows the user
 * to specify a collection of filters that query the set of all tracks.
 * 
 * @author Jon
 *
 */
public class FiltersWindow
{

    //---------------- Private variables -----------------------------------
	
    private Window filtersWindow = null;
	private Skins skins = null;
	private int textInputYCoordinate = -1;
	private int plusButtonYCoordinate = -1;
	private int minusButtonYCoordinate = -1;
	private int complexButtonYCoordinate = -1;
	private FilterCollection filterCollection = null;
	private Logger uiLogger = null;
	private Logger filterLogger = null;
	
	/*
	 * BXML variables.
	 */
	@BXML private Border filtersBorder = null;
	@BXML private TablePane filterTablePane = null;
	@BXML private Border actionBorder = null;
	@BXML private BoxPane actionBoxPane = null;
	@BXML private PushButton showResultsButton = null;
	@BXML private PushButton queryDoneButton = null;
    
    /**
     * Class constructor.
     */
    public FiltersWindow ()
    {
    	
    	/*
    	 * Create a UI logger.
    	 */
    	String className = getClass().getSimpleName();
    	uiLogger = (Logger) LoggerFactory.getLogger(className + "_UI");
    	
    	/*
    	 * Create a filter logger.
    	 */
    	filterLogger = (Logger) LoggerFactory.getLogger(className + "_Filter");
    	
    	/*
    	 * Get the logging object singleton.
    	 */
    	Logging logging = Logging.getInstance();
    	
    	/*
    	 * Register our loggers.
    	 */
    	logging.registerLogger(Logging.Dimension.UI, uiLogger);
    	logging.registerLogger(Logging.Dimension.FILTER, filterLogger);
    	
    	/*
    	 * Initialize variables.
    	 */
    	skins = Skins.getInstance();
		
    	uiLogger.trace("FiltersWindow constructor: " + this.hashCode());
    }

    //---------------- Public methods --------------------------------------
    
	/**
	 * Displays the filters in a new window.
	 * 
	 * @param display display object for managing windows
	 * @throws IOException If an error occurs trying to read the BXML file.
	 * @throws SerializationException If an error occurs trying to 
	 * deserialize the BXML file.
	 */
    public void displayFilters (Display display) 
    		throws IOException, SerializationException
    {
    	uiLogger.trace("displayFilters: " + this.hashCode());
    	
    	/*
    	 * Get the BXML information for the filters window, and generate the list of components
    	 * to be skinned.
    	 */
		List<Component> components = new ArrayList<Component>();
		initializeBxmlVariables(components);
        
        /*
         * Listener to handle the show results button press.
         */
        showResultsButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	uiLogger.info("show results button pressed");
            	
            	/*
            	 * Collect the filters from the filter window.
            	 */
            	collectFilters();
            	
            	/*
            	 * Continue if we have a valid collection.
            	 */
            	if (filterCollection != null)
            	{
        			
        			/*
        			 * Execute the set of filters.
        			 */
					if (filterCollection.executeFilterList() == true)
					{
					
						/*
						 * Get the resulting filtered tracks, if any.
						 */
						List<Track> filteredTracks = filterCollection.getFilteredTracks();
						int numTracks = filteredTracks.getLength();

						filterLogger.info((Integer.toString(numTracks) 
								+ ((numTracks == 1) ? " track matches" : " tracks match") 
								+ " the set of filters"));

						/*
						 * If anything is to be displayed, uh, display it.
						 */
						if (!filteredTracks.isEmpty())
						{
							try
							{
								String queryStr = filterCollection.getFiltersAsString();
								TracksWindow tracksWindowHandler = new TracksWindow();
								tracksWindowHandler.saveWindowAttributes(TracksWindow.QueryType.TRACKS, 
										TracksWindow.QueryType.TRACKS.getDisplayValue() + ": " + queryStr,
										TrackDisplayColumns.ColumnSet.FILTERED_VIEW.getNamesList());
								tracksWindowHandler.displayTracks(display, filteredTracks, null);
							}
							catch (IOException | SerializationException e)
							{
								filterLogger.error("caught " + e.getClass().getSimpleName());
								e.printStackTrace();
							}
						}
						else
						{
							Alert.alert(MessageType.INFO, 
									StringConstants.ALERT_NO_TRACKS, filtersWindow);
						}
					}
					
					/*
					 * Oh no Mr. Bill! Something is wrong with one or more filters.
					 */
					else
					{
						Alert.alert(MessageType.ERROR, 
								StringConstants.ALERT_FILTER_ERROR + 
								filterCollection.getFilterErrorString(), filtersWindow);
					}
            	}
            }
        });
        
        /*
         * Listener to handle the done button press.
         */
        queryDoneButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	uiLogger.info("done button pressed");
            	
            	/*
            	 * Close the window.
            	 */
            	filtersWindow.close();
            	
            	/*
            	 * Pop the window off the skins window stack.
            	 */
            	skins.popSkinnedWindow();
            }
        });
        
        /*
         * Add widget texts.
         */
        showResultsButton.setButtonData(StringConstants.FILTER_SHOW_ME_BUTTON);
        showResultsButton.setTooltipText(StringConstants.FILTER_SHOW_ME_BUTTON_TIP);
        queryDoneButton.setButtonData(StringConstants.DONE);
		
		/*
		 * Set the window title.
		 */
		filtersWindow.setTitle(Skins.Window.FILTERS.getDisplayValue());
        
        /*
         * Add the initial filter row. This populates the component list with table row components.
         */
    	TablePane.Row newRow = createFilterTableRow(true, components);
    	filterTablePane.getRows().add(newRow);
		
    	/*
    	 * Now register the filters window skin elements.
    	 */
    	Map<Skins.Element, List<Component>> windowElements = skins.mapComponentsToSkinElements(components);
		skins.registerWindowElements(Skins.Window.FILTERS, windowElements);
		
		/*
		 * Skin the filters window.
		 */
		skins.skinMe(Skins.Window.FILTERS);
		
		/*
		 * Push the skinned window onto the skins window stack. It gets popped from our done button press
		 * handler.
		 */
		skins.pushSkinnedWindow(Skins.Window.FILTERS);
    	
    	/*
    	 * Open the filters window.
    	 */
    	uiLogger.info("opening filters window");
        filtersWindow.open(display);
    	
    	/*
    	 * Request focus for the table pane. This gives focus to the first component in the row, which
    	 * is the logic spinner. Ideally, I'd like the text input to be focused, but that isn't 
    	 * possible.
    	 */
        filterTablePane.requestFocus();
    }

    //---------------- Private methods -------------------------------------
    
    /*
     * Create and add a filter row.
     * 
     * NOTE: This method populates the input list of components with the components of a row.
     */
    private TablePane.Row createFilterTableRow (boolean includeLogicSpinner, List<Component> components)
    {
		uiLogger.trace("createFilterTableRow: " + this.hashCode());
        
    	/*
    	 * NOTE:
    	 * We need the ability to operate on filter rows based on the specific table row that the 
    	 * user is using. But this a bit of a complex dance. To use a button example, the buttonPressed()
    	 * method does not provide a means to know on which row the button exists. So we need a
    	 * ComponentMouseButtonListener to get that function. However, based on the hierarchy of 
    	 * listener calls, and the bubbling up of events through the component hierarchy, the 
    	 * buttonPressed() method gets called before the mouseClick() method. :(
    	 * 
    	 * So the buttonPressed() method just records the Y coordinate of the button in a class
    	 * variable, which is relative to its parent component, which is the TablePane.
    	 * 
    	 * Then the mouseClick() method gets the parent, uses the recorded Y coordinate to get the
    	 * table row, then uses that information to operate on the row.
    	 */
    	
    	/*
    	 * New table row object.
    	 */
    	TablePane.Row newRow = new TablePane.Row();
    	
    	/*
    	 * Create a cell filler.
    	 */
		TablePane.Filler fill = new TablePane.Filler();
    	
    	/*
    	 * Create the 'logic' spinner.
    	 */
    	Spinner logic = new Spinner();
    	logic.setCircular(true);
    	logic.setSpinnerData(Filter.getLogicLabels());
    	logic.setSelectedIndex(0);
    	logic.setTooltipText(StringConstants.FILTER_LOGIC_TIP);
    	
    	/*
    	 * Create the 'subject' spinner.
    	 */
    	Spinner subject = new Spinner();
    	subject.setCircular(true);
    	subject.setSpinnerData(Filter.getSubjectLabels());
        subject.setSelectedIndex(0);
        subject.setTooltipText(StringConstants.FILTER_SUBJECT_TIP);
        
        /*
         * Create the 'operator' spinner.
         */
    	Spinner operator = new Spinner();
    	operator.setCircular(true);
    	operator.setSpinnerData(Filter.getOperatorLabels());
        operator.setSelectedIndex(0);
        operator.setTooltipText(StringConstants.FILTER_OPERATOR_TIP);
        
        /*
         * Create the text input box.
         */
    	TextInput text = new TextInput();
    	text.setTooltipText(StringConstants.FILTER_TEXT_TIP);
    	
    	/*
    	 * Text input listener for the text input. We call the typing assistant to fill in the 
    	 * artist name as soon as enough characters are entered.
    	 */
    	text.getTextInputContentListeners().add(new TextInputContentListener.Adapter()
    	{
            @Override
            public void textInserted(TextInput textInput, int index, int count)
            {
            	textInputYCoordinate = textInput.getY();
            	Object parent = textInput.getParent();
            	
            	if (parent instanceof TablePane)
            	{
            		TablePane tablePane = (TablePane) parent;
                	
                	/*
                	 * Indexes into the row elements.
                	 * 
                	 * IMPORTANT: These must match the design of the row. See filtersWindow.bxml 
                	 * for the column definition, and createFilterTableRow() for the logic to create 
                	 * a row.
                	 */
                	final int subjectIndex  = 1;
                	final int operatorIndex = 2;

                	/*
                	 * Get the row we are dealing with.
                	 */
            		TablePane.RowSequence rows = tablePane.getRows();
            		int filterRowIndex = tablePane.getRowAt(textInputYCoordinate);
            		TablePane.Row row = rows.get(filterRowIndex);
            		
            		/*
            		 * We only care if the subject is artist.
            		 */
        			Spinner subject = (Spinner) row.get(subjectIndex);
        			String subjectValue = (String) subject.getSelectedItem();
            		if (Filter.Subject.getEnum(subjectValue).equals(Filter.Subject.ARTIST))
            		{
            			
            			/*
            			 * The only operators we support for string values such as artist are 
            			 * IS and CONTAINS. We need to pass the operator to the typing assistant
            			 * so it can do the right thing.
            			 */
            			Spinner operator = (Spinner) row.get(operatorIndex);
            			String operatorValue = (String) operator.getSelectedItem();
            			Filter.Operator operatorEnum = Filter.Operator.getEnum(operatorValue);
            			
                		Utilities.typingAssistant(textInput, XMLHandler.getArtistNames(),
                				textInput.getText(), operatorEnum);
            		}
            	}
            }    		
    	});
    	
    	/*
    	 * Create the set of buttons:
    	 * 
    	 * '+'       = insert a new row after this one
    	 * '-'       = delete this row
    	 * 'Complex' = insert a new sub-group after this row
    	 */
    	PushButton plusButton = new PushButton();
    	plusButton.setButtonData("+");
    	plusButton.setTooltipText(StringConstants.FILTER_PLUS_BUTTON_TIP);
    	
    	PushButton minusButton = new PushButton();
    	minusButton.setButtonData("-");
    	minusButton.setTooltipText(StringConstants.FILTER_MINUS_BUTTON_TIP);
    	
    	PushButton complexButton = new PushButton();
    	complexButton.setButtonData(StringConstants.FILTER_COMPLEX);
    	complexButton.setTooltipText(StringConstants.FILTER_COMPLEX_BUTTON_TIP);
    	
    	/*
    	 * Mouse click listener for the + button.
    	 */
    	plusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
		{
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
            	Object parent = component.getParent();
            	if (parent instanceof TablePane)
            	{
            		TablePane tablePane = (TablePane) parent;
                    int filterRowIndex = tablePane.getRowAt(plusButtonYCoordinate);
                    uiLogger.info("plus button pressed for filter index " + filterRowIndex);
                    
                    /*
                     * Add the table row and collect the components that need to be skinned.
                     */
                    List<Component> rowComponents = new ArrayList<Component>();
                	TablePane.Row tableRow = createFilterTableRow(false, rowComponents);
                	filterTablePane.getRows().insert(tableRow, filterRowIndex + 1);
            		
                	/*
                	 * Register the new components and skin them.
                	 */
            		Map<Skins.Element, List<Component>> windowElements = 
            				skins.mapComponentsToSkinElements(rowComponents);            		
            		skins.registerDynamicWindowElements(Skins.Window.FILTERS, windowElements);
            		skins.skinMe(Skins.Window.FILTERS);
                	
                	filtersWindow.repaint();
            	}
 
                return false;
            }
        });
    	
    	/*
    	 * Button press listener for the + button.
    	 */
    	plusButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	plusButtonYCoordinate = button.getY();
            }
        });
    	
    	/*
    	 * Mouse click listener for the - button.
    	 */
    	minusButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
		{
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
            	Object parent = component.getParent();
            	if (parent instanceof TablePane)
            	{
            		TablePane tablePane = (TablePane) parent;
                    int filterRowIndex = tablePane.getRowAt(minusButtonYCoordinate);
                    uiLogger.info("minus button pressed for filter index " + filterRowIndex);
                	filterTablePane.getRows().remove(filterRowIndex, 1);
                	
                	filtersWindow.repaint();
            	}
 
                return false;
            }
        });

    	/*
    	 * Button press listener for the - button.
    	 */
        minusButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	minusButtonYCoordinate = button.getY();
            }
        });
    	
    	/*
    	 * Mouse click listener for the Complex button.
    	 */
    	complexButton.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter()
		{
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count)
            {
            	Object parent = component.getParent();
            	if (parent instanceof TablePane)
            	{
            		TablePane tablePane = (TablePane) parent;
                    int filterRowIndex = tablePane.getRowAt(complexButtonYCoordinate);
                    uiLogger.info("complex button pressed for filter index " + filterRowIndex);
                    
                    /*
                     * Add the table row and collect the components that need to be skinned.
                     */
                    List<Component> rowComponents = new ArrayList<Component>();
                	TablePane.Row tableRow = createFilterTableRow(true, rowComponents);
                	filterTablePane.getRows().insert(tableRow, filterRowIndex + 1); 
            		
                	/*
                	 * Register the new components and skin them.
                	 */
            		Map<Skins.Element, List<Component>> windowElements = 
            				skins.mapComponentsToSkinElements(rowComponents);            		
            		skins.registerDynamicWindowElements(Skins.Window.FILTERS, windowElements);
            		skins.skinMe(Skins.Window.FILTERS);  
                	
                	filtersWindow.repaint();
            	}
 
                return false;
            }
        });
    	
    	/*
    	 * Button press listener for the Complex button.
    	 */
    	complexButton.getButtonPressListeners().add(new ButtonPressListener() 
        {
            @Override
            public void buttonPressed(Button button) 
            {
            	complexButtonYCoordinate = button.getY();
            }
        });

        /*
         * Assemble the new row.
         */    	
    	if (includeLogicSpinner == true)
    	{
    		newRow.add(logic);
    		components.add(logic);
    	}
    	else
    	{
    		newRow.add(fill);
    		components.add(fill);
    	}
    	
    	newRow.add(subject);
		components.add(subject);
    	newRow.add(operator);
		components.add(operator);
    	newRow.add(text);
		components.add(text);
    	newRow.add(plusButton);
		components.add(plusButton);
    	newRow.add(minusButton);
		components.add(minusButton);
    	newRow.add(complexButton);
		components.add(complexButton);
    	
    	return newRow;
    }
    
    /*
     * Collect the entered filters and create a filter collection object.
     */
    private void collectFilters ()
    {
		filterLogger.trace("collectFilters: " + this.hashCode());
    	
    	/*
    	 * Indexes into the row elements.
    	 * 
    	 * IMPORTANT: These must match the design of the row. See filtersWindow.bxml for the column
    	 * definition, and createFilterTableRow() for the logic to create a row.
    	 */
    	final int logicIndex    = 0;
    	final int subjectIndex  = 1;
    	final int operatorIndex = 2;
    	final int textIndex     = 3;
    	
    	/*
    	 * Initialize a new filter collection object.
    	 */
    	filterCollection = new FilterCollection();
    	
    	/*
    	 * Iterate through the filter table rows.
    	 */
    	TablePane.RowSequence rows = filterTablePane.getRows();
    	Iterator<TablePane.Row> rowsIterator = rows.iterator();
    	while (rowsIterator.hasNext())
    	{
    		TablePane.Row row = rowsIterator.next();
    		
    		/*
    		 * Initialize a new filter object.
    		 */
    		Filter filter = new Filter();
    		
    		/*
    		 * Handle the logic spinner if it exists.
    		 */
    		Component cell = row.get(logicIndex);
			if (cell instanceof Spinner)
			{
				Spinner logic = (Spinner) cell;
				String logicValue = (String) logic.getSelectedItem();
				filter.setFilterLogic(Filter.Logic.getEnum(logicValue));
			}
			
			/*
			 * Handle the subject spinner.
			 */
			Spinner subject = (Spinner) row.get(subjectIndex);
			String subjectValue = (String) subject.getSelectedItem();
			filter.setFilterSubject(Filter.Subject.getEnum(subjectValue));
			
			/*
			 * Handle the operator spinner.
			 */
			Spinner operator = (Spinner) row.get(operatorIndex);
			String operatorValue = (String) operator.getSelectedItem();
			filter.setFilterOperator(Filter.Operator.getEnum(operatorValue));
			
			/*
			 * Handle the text input.
			 */
			TextInput text = (TextInput) row.get(textIndex);
			filter.setFilterText(text.getText());
			
			/*
			 * Add this filter to the collection.
			 */
			filterCollection.addFilter(filter);
    	}
    }
    
    /*
     * Initialize BXML variables and collect the list of components to be skinned.
     */
    private void initializeBxmlVariables (List<Component> components) 
    		throws IOException, SerializationException
    {
		uiLogger.trace("initializeBxmlVariables: " + this.hashCode());
		
        BXMLSerializer windowSerializer = new BXMLSerializer();
        filtersWindow = (Window)windowSerializer.
        		readObject(getClass().getResource("filtersWindow.bxml"));
        
        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = (MenuBars)filtersWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, false);

        filtersBorder = 
        		(Border)windowSerializer.getNamespace().get("filtersBorder");
		components.add(filtersBorder);
        filterTablePane = 
        		(TablePane)windowSerializer.getNamespace().get("filterTablePane");
		components.add(filterTablePane);
        actionBorder = 
        		(Border)windowSerializer.getNamespace().get("actionBorder");
		components.add(actionBorder);
        actionBoxPane = 
        		(BoxPane)windowSerializer.getNamespace().get("actionBoxPane");
		components.add(actionBoxPane);
        showResultsButton = 
        		(PushButton)windowSerializer.getNamespace().get("showResultsButton");
		components.add(showResultsButton);
        queryDoneButton = 
        		(PushButton)windowSerializer.getNamespace().get("queryDoneButton");
		components.add(queryDoneButton);
    }
}
