package itunesq;

import java.io.IOException;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
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
 * Class that handles the display filters window. This window allows the user to
 * specify a collection of filters that query the set of all tracks.
 * 
 * @author Jon
 *
 */
public class FiltersWindow
{

    // ---------------- Private variables -----------------------------------

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
    public FiltersWindow()
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

    // ---------------- Public methods --------------------------------------

    /**
     * Displays the filters in a new window.
     * 
     * @param display display object for managing windows
     * @throws IOException If an error occurs trying to read the BXML file.
     * @throws SerializationException If an error occurs trying to deserialize
     * the BXML file.
     */
    public void displayFilters(Display display) 
            throws IOException, SerializationException
    {
        uiLogger.trace("displayFilters: " + this.hashCode());

        if (display == null)
        {
            throw new IllegalArgumentException("display argument is null");
        }

        /*
         * Get the BXML information for the filters window, and generate the
         * list of components to be skinned.
         */
        List<Component> components = new ArrayList<Component>();
        initializeBxmlVariables(components);

        /*
         * Set up the various event handlers.
         */
        createEventHandlers(display);

        /*
         * Add the initial filter row. This populates the component list with
         * table row components.
         */
        TablePane.Row newRow = createFilterTableRow(true, components);
        filterTablePane.getRows().add(newRow);

        /*
         * Add widget texts.
         */
        showResultsButton.setButtonData(StringConstants.FILTER_SHOW_ME_BUTTON);
        showResultsButton.setTooltipText(StringConstants.FILTER_SHOW_ME_BUTTON_TIP);
        showResultsButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);
        queryDoneButton.setButtonData(StringConstants.DONE);

        /*
         * Set the window title.
         */
        filtersWindow.setTitle(Skins.Window.FILTERS.getDisplayValue());

        /*
         * Now register the filters window skin elements.
         */
        skins.registerWindowElements(Skins.Window.FILTERS, components);

        /*
         * Skin the filters window.
         */
        skins.skinMe(Skins.Window.FILTERS);

        /*
         * Push the skinned window onto the skins window stack. It gets popped
         * from our done button press handler.
         */
        skins.pushSkinnedWindow(Skins.Window.FILTERS);

        /*
         * Open the filters window.
         */
        uiLogger.info("opening filters window");
        filtersWindow.open(display);

        /*
         * Request focus for the text input on the filter table row.
         */
        for (Component rowComponent : newRow)
        {
            if (rowComponent instanceof TextInput)
            {
                rowComponent.requestFocus();
            }
        }
    }

    // ---------------- Private methods -------------------------------------

    /*
     * Set up the various event handlers.
     */
    private void createEventHandlers(Display display)
    {
        uiLogger.trace("createEventHandlers: " + this.hashCode());

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
                            String queryStr = filterCollection.getFiltersAsString();
                            TracksWindow tracksWindowHandler = new TracksWindow();
                            tracksWindowHandler.saveWindowAttributes(ListQueryType.Type.TRACK_QUERY,
                                    ListQueryType.Type.TRACK_QUERY.getDisplayValue() + ": " + queryStr,
                                    TrackDisplayColumns.ColumnSet.FILTERED_VIEW.getNamesList());

                            try
                            {
                                tracksWindowHandler.displayTracks(display, Skins.Window.TRACKS, filteredTracks, null);
                            }
                            catch (IOException | SerializationException e)
                            {
                                MainWindow.logException(filterLogger, e);
                                throw new InternalErrorException(true, e.getMessage());
                            }
                        }
                        else
                        {
                            Alert.alert(MessageType.INFO, StringConstants.ALERT_NO_TRACKS, filtersWindow);
                        }
                    }

                    /*
                     * Oh no Mr. Bill! Something is wrong with one or more
                     * filters.
                     */
                    else
                    {
                        Alert.alert(MessageType.ERROR,
                                StringConstants.ALERT_FILTER_ERROR + filterCollection.getFilterErrorString(),
                                filtersWindow);
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
    }

    /*
     * Create and add a filter row.
     * 
     * NOTE: This method populates the input list of components with the
     * components of a row.
     */
    private TablePane.Row createFilterTableRow(boolean includeLogicSpinner, List<Component> components)
    {
        uiLogger.trace("createFilterTableRow: " + this.hashCode());

        /*
         * NOTE: We need the ability to operate on filter rows based on the
         * specific table row that the user is using. But this a bit of a
         * complex dance. To use a button example, the buttonPressed() method
         * does not provide a means to know on which row the button exists. So
         * we need a ComponentMouseButtonListener to get that function. However,
         * based on the hierarchy of listener calls, and the bubbling up of
         * events through the component hierarchy, the buttonPressed() method
         * gets called before the mouseClick() method. :(
         * 
         * So the buttonPressed() method just records the Y coordinate of the
         * button in a class variable, which is relative to its parent
         * component, which is the TablePane.
         * 
         * Then the mouseClick() method gets the parent, uses the recorded Y
         * coordinate to get the table row, then uses that information to
         * operate on the row.
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
        logic.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * Create the 'subject' spinner.
         */
        Spinner subject = new Spinner();
        subject.setCircular(true);
        subject.setSpinnerData(Filter.getSubjectLabels());
        subject.setSelectedIndex(0);
        subject.setTooltipText(StringConstants.FILTER_SUBJECT_TIP);
        subject.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * Create the 'operator' spinner.
         */
        Spinner operator = new Spinner();
        operator.setCircular(true);
        operator.setSpinnerData(Filter.getOperatorLabels());
        operator.setSelectedIndex(0);
        operator.setTooltipText(StringConstants.FILTER_OPERATOR_TIP);
        operator.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * Create the text input box.
         */
        TextInput text = new TextInput();
        text.setTooltipText(StringConstants.FILTER_TEXT_TIP);
        text.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        /*
         * Text input listener for the text input. We call the typing assistant
         * to fill in the artist name as soon as enough characters are entered.
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
                     * IMPORTANT: These must match the design of the row. See
                     * filtersWindow.bxml for the column definition, and
                     * createFilterTableRow() for the logic to create a row.
                     */
                    final int subjectIndex = 1;
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
                         * The only operators we support for string values such
                         * as artist are IS and CONTAINS. We need to pass the
                         * operator to the typing assistant so it can do the
                         * right thing.
                         */
                        Spinner operator = (Spinner) row.get(operatorIndex);
                        String operatorValue = (String) operator.getSelectedItem();
                        Filter.Operator operatorEnum = Filter.Operator.getEnum(operatorValue);

                        /*
                         * Create an array of artist display names for the typing assistant.
                         * Unfortunately, we don't have such a list handy.
                         */
                        ArrayList<String> artistNames = new ArrayList<String>();
                        artistNames.setComparator(String.CASE_INSENSITIVE_ORDER);

                        ArrayList<ArtistCorrelator> artistCorrs = Database.getArtistCorrelators();

                        for (ArtistCorrelator artistCorr : artistCorrs)
                        {
                            artistNames.add(artistCorr.getDisplayName());
                        }

                        Utilities.typingAssistant(textInput, artistNames, textInput.getText(),
                                operatorEnum);
                    }
                }
            }
        });

        /*
         * Create the set of buttons:
         * 
         * '+' = insert a new row after this one '-' = delete this row 'Complex'
         * = insert a new sub-group after this row
         */
        PushButton plusButton = new PushButton();
        plusButton.setButtonData("+");
        plusButton.setTooltipText(StringConstants.FILTER_PLUS_BUTTON_TIP);
        plusButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        PushButton minusButton = new PushButton();
        minusButton.setButtonData("-");
        minusButton.setTooltipText(StringConstants.FILTER_MINUS_BUTTON_TIP);
        minusButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

        PushButton complexButton = new PushButton();
        complexButton.setButtonData(StringConstants.FILTER_COMPLEX);
        complexButton.setTooltipText(StringConstants.FILTER_COMPLEX_BUTTON_TIP);
        complexButton.setTooltipDelay(InternalConstants.TOOLTIP_DELAY);

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
                     * Add the table row and collect the components that need to
                     * be skinned.
                     */
                    List<Component> rowComponents = new ArrayList<Component>();
                    TablePane.Row tableRow = createFilterTableRow(false, rowComponents);
                    filterTablePane.getRows().insert(tableRow, filterRowIndex + 1);

                    /*
                     * Request focus for the text input on the newly added row.
                     */
                    for (Component rowComponent : tableRow)
                    {
                        if (rowComponent instanceof TextInput)
                        {
                            rowComponent.requestFocus();
                        }
                    }

                    /*
                     * Register the new components and skin them.
                     */
                    Map<Skins.Element, List<Component>> windowElements = 
                            skins.registerDynamicWindowElements(Skins.Window.FILTERS, rowComponents);
                    skins.skinMe(Skins.Window.FILTERS, windowElements);

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

                    /*
                     * Get the number of rows and make sure we don't go below
                     * one row.
                     */
                    int numRows = tablePane.getRows().getLength();
                    if (numRows <= 1)
                    {
                        Alert.alert(MessageType.ERROR, StringConstants.ALERT_FILTER_TOO_FEW_ROWS,
                                component.getWindow());
                    }
                    else
                    {

                        /*
                         * Remove the table row.
                         */
                        Sequence<TablePane.Row> removedRows = 
                                filterTablePane.getRows().remove(filterRowIndex, 1);

                        /*
                         * Get the remaining rows.
                         */
                        Sequence<TablePane.Row> remainingRows = filterTablePane.getRows();

                        /*
                         * Get the new number of rows.
                         */
                        numRows = remainingRows.getLength();

                        /*
                         * If we didn't remove the last row in the table, we
                         * need to check if the one we removed contained a logic
                         * spinner.
                         */
                        if (numRows > filterRowIndex)
                        {

                            /*
                             * Get the removed row from the sequence (of 1) that
                             * we removed.
                             */
                            TablePane.Row removedRow = removedRows.get(0);

                            /*
                             * Get the first component on the row, which is a
                             * spinner or a filler.
                             */
                            Component spinnerOrFiller = removedRow.get(0);

                            /*
                             * Continue if we removed a row with a spinner.
                             */
                            if (spinnerOrFiller instanceof Spinner)
                            {
                                Spinner removedSpinner = (Spinner) spinnerOrFiller;

                                /*
                                 * Get the row after the one we removed, which
                                 * is at the same row index, then get its
                                 * spinner or filler.
                                 */
                                TablePane.Row successiveRow = remainingRows.get(filterRowIndex);
                                spinnerOrFiller = successiveRow.get(0);

                                /*
                                 * If the successive row contains a filler, then
                                 * update it with the logic spinner from the row
                                 * we removed.
                                 */
                                if (spinnerOrFiller instanceof TablePane.Filler)
                                {
                                    successiveRow.update(0, removedSpinner);

                                    /*
                                     * Pivot doesn't support update
                                     * (contradicting their doc), so we have to
                                     * remove then insert the updated row.
                                     */
                                    remainingRows.remove(successiveRow);
                                    remainingRows.insert(successiveRow, filterRowIndex);
                                }
                            }
                        }

                        filtersWindow.repaint();
                    }
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
                     * Add the table row and collect the components that need to
                     * be skinned.
                     */
                    List<Component> rowComponents = new ArrayList<Component>();
                    TablePane.Row tableRow = createFilterTableRow(true, rowComponents);
                    filterTablePane.getRows().insert(tableRow, filterRowIndex + 1);

                    /*
                     * Request focus for the text input on the newly added row.
                     */
                    for (Component rowComponent : tableRow)
                    {
                        if (rowComponent instanceof TextInput)
                        {
                            rowComponent.requestFocus();
                        }
                    }

                    /*
                     * Register the new components and skin them.
                     */
                    skins.registerDynamicWindowElements(Skins.Window.FILTERS, rowComponents);
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
    private void collectFilters()
    {
        filterLogger.trace("collectFilters: " + this.hashCode());

        /*
         * Indexes into the row elements.
         * 
         * IMPORTANT: These must match the design of the row. See
         * filtersWindow.bxml for the column definition, and
         * createFilterTableRow() for the logic to create a row.
         */
        final int logicIndex = 0;
        final int subjectIndex = 1;
        final int operatorIndex = 2;
        final int textIndex = 3;

        /*
         * Initialize a new filter collection object.
         */
        filterCollection = new FilterCollection();

        /*
         * Iterate through the filter table rows.
         */
        TablePane.RowSequence rows = filterTablePane.getRows();
        for (TablePane.Row row : rows)
        {

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
     * Initialize BXML variables and collect the list of components to be
     * skinned.
     */
    private void initializeBxmlVariables(List<Component> components)
            throws IOException, SerializationException
    {
        uiLogger.trace("initializeBxmlVariables: " + this.hashCode());

        BXMLSerializer windowSerializer = new BXMLSerializer();

        filtersWindow =
                (Window) windowSerializer.readObject(getClass().getResource("filtersWindow.bxml"));

        /*
         * Initialize the menu bar.
         */
        MenuBars menuBar = (MenuBars) filtersWindow;
        menuBar.initializeMenuBxmlVariables(windowSerializer, components, false);

        filtersBorder = 
                (Border) windowSerializer.getNamespace().get("filtersBorder");
        components.add(filtersBorder);
        filterTablePane = 
                (TablePane) windowSerializer.getNamespace().get("filterTablePane");
        components.add(filterTablePane);
        actionBorder = 
                (Border) windowSerializer.getNamespace().get("actionBorder");
        components.add(actionBorder);
        actionBoxPane = 
                (BoxPane) windowSerializer.getNamespace().get("actionBoxPane");
        components.add(actionBoxPane);
        showResultsButton = 
                (PushButton) windowSerializer.getNamespace().get("showResultsButton");
        components.add(showResultsButton);
        queryDoneButton = 
                (PushButton) windowSerializer.getNamespace().get("queryDoneButton");
        components.add(queryDoneButton);
    }
}
