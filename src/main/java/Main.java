import edu.sdccd.cisc.Dog;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private Dog formRow = new Dog("", "", 0);
    private TableView<Dog> table;

    // App-managed state with arrays (no collections):
    private boolean[] editingRows;        // per-index "is editing"
    private TextField[] nameFields;       // per-index editor refs
    private TextField[] breedFields;
    private TextField[] ageFields;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Dog List Application");

        table = new TableView<>();
        table.setEditable(false);

        // Initial data as a Dog[] (no collections)
        Dog[] dogs = new Dog[] {
                new Dog("Buddy",  "Golden Retriever", 5),
                new Dog("Max",    "German Shepherd",  3),
                new Dog("Bella",  "Labrador",         2),
                new Dog("Charlie","Beagle",           4),
                new Dog("Lucy",   "Poodle",           6),
                formRow // last "form" row
        };

        // Initialize array-backed UI state sized to data length
        int n = dogs.length;
        editingRows = new boolean[n];
        nameFields  = new TextField[n];
        breedFields = new TextField[n];
        ageFields   = new TextField[n];

        // JavaFX still needs an ObservableList to display.
        ObservableList<Dog> dogList = FXCollections.observableArrayList(dogs);

        // Name column
        TableColumn<Dog, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(150);
        nameCol.setCellFactory(param -> new TableCell<Dog, String>() {
            private final TextField textField = new TextField();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                int idx = getIndex();
                Dog dog = getTableView().getItems().get(idx);
                if (dog == formRow || editingRows[idx]) {
                    textField.setText(dog.getName());
                    nameFields[idx] = textField;
                    setGraphic(textField);
                } else {
                    setGraphic(new Label(dog.getName()));
                }
            }
        });

        // Breed column
        TableColumn<Dog, String> breedCol = new TableColumn<>("Breed");
        breedCol.setPrefWidth(150);
        breedCol.setCellFactory(param -> new TableCell<Dog, String>() {
            private final TextField textField = new TextField();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                int idx = getIndex();
                Dog dog = getTableView().getItems().get(idx);
                if (dog == formRow || editingRows[idx]) {
                    textField.setText(dog.getBreed());
                    breedFields[idx] = textField;
                    setGraphic(textField);
                } else {
                    setGraphic(new Label(dog.getBreed()));
                }
            }
        });

        // Age column
        TableColumn<Dog, Number> ageCol = new TableColumn<>("Age");
        ageCol.setPrefWidth(80);
        ageCol.setCellFactory(param -> new TableCell<Dog, Number>() {
            private final TextField textField = new TextField();
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                int idx = getIndex();
                Dog dog = getTableView().getItems().get(idx);
                if (dog == formRow || editingRows[idx]) {
                    textField.setText(Integer.toString(dog.getAge()));
                    ageFields[idx] = textField;
                    setGraphic(textField);
                } else {
                    setGraphic(new Label(Integer.toString(dog.getAge())));
                }
            }
        });

        // Action column
        TableColumn<Dog, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(param -> new TableCell<Dog, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button saveBtn = new Button("Save");
            private final Button cancelBtn = new Button("Cancel");
            private final Button deleteBtn = new Button("Delete");
            private final Button addBtn = new Button("Add");
            private final HBox viewBox = new HBox(8);
            private final HBox editBox = new HBox(8);

            {
                viewBox.getChildren().addAll(editBtn, deleteBtn);
                viewBox.setAlignment(Pos.CENTER_LEFT);
                editBox.getChildren().addAll(saveBtn, cancelBtn);
                editBox.setAlignment(Pos.CENTER_LEFT);

                editBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < editingRows.length && dogList.get(idx) != formRow) {
                        editingRows[idx] = true;
                        table.refresh();
                    }
                });

                saveBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < editingRows.length) {
                        Dog d = dogList.get(idx);
                        if (d == formRow) {
                            // Add new dog from form fields
                            String name = nameFields[idx] != null ? nameFields[idx].getText() : "";
                            String breed = breedFields[idx] != null ? breedFields[idx].getText() : "";
                            int age = 0;
                            try { age = Integer.parseInt(ageFields[idx] != null ? ageFields[idx].getText() : "0"); } catch (Exception ex) {}
                            // Insert above the form row; arrays can't grow, but the UI list can.
                            Dog newDog = new Dog(name, breed, age);
                            int insertAt = dogList.size() - 1;
                            dogList.add(insertAt, newDog);
                            // refresh array-backed flags by resizing (+1)
                            growArraysByOne();
                            table.refresh();
                        } else {
                            // Save edits
                            String name = nameFields[idx] != null ? nameFields[idx].getText() : d.getName();
                            String breed = breedFields[idx] != null ? breedFields[idx].getText() : d.getBreed();
                            int age = d.getAge();
                            try { age = Integer.parseInt(ageFields[idx] != null ? ageFields[idx].getText() : Integer.toString(age)); } catch (Exception ex) {}
                            d.setName(name);
                            d.setBreed(breed);
                            d.setAge(age);
                            editingRows[idx] = false;
                            table.refresh();
                        }
                    }
                });

                cancelBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < editingRows.length) {
                        editingRows[idx] = false;
                        table.refresh();
                    }
                });

                deleteBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < dogList.size()) {
                        if (dogList.get(idx) != formRow) {
                            dogList.remove(idx);
                            shrinkArraysByOne(idx);
                            table.refresh();
                        }
                    }
                });

                addBtn.setOnAction(e -> {
                    // Add when on the form row (last row)
                    int idx = getIndex();
                    if (idx == dogList.size() - 1) {
                        String name  = nameFields[idx]  != null ? nameFields[idx].getText() : "";
                        String breed = breedFields[idx] != null ? breedFields[idx].getText() : "";
                        int age = 0;
                        try { age = Integer.parseInt(ageFields[idx] != null ? ageFields[idx].getText() : "0"); } catch (Exception ex) {}
                        Dog newDog = new Dog(name, breed, age);
                        int insertAt = dogList.size() - 1;
                        dogList.add(insertAt, newDog);
                        growArraysByOne();
                        // Clear the form inputs
                        if (nameFields[idx] != null)  nameFields[idx].clear();
                        if (breedFields[idx] != null) breedFields[idx].clear();
                        if (ageFields[idx] != null)   ageFields[idx].clear();
                        table.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                int idx = getIndex();
                Dog d = getTableView().getItems().get(idx);
                if (d == formRow) {
                    setGraphic(addBtn);
                } else if (idx >= 0 && idx < editingRows.length && editingRows[idx]) {
                    setGraphic(editBox);
                } else {
                    setGraphic(viewBox);
                }
            }
        });

        table.getColumns().addAll(nameCol, breedCol, ageCol, actionCol);
        table.setItems(dogList);

        Label titleLabel = new Label("Dog List (Arrays, no app-level collections)");
        VBox vbox = new VBox(10, titleLabel, table);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 640, 460);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** Expand the per-row arrays by one to match a newly added data row. */
    private void growArraysByOne() {
        int n = editingRows.length + 1;
        boolean[] e2 = new boolean[n];
        System.arraycopy(editingRows, 0, e2, 0, editingRows.length);
        editingRows = e2;

        TextField[] n2 = new TextField[n];
        System.arraycopy(nameFields, 0, n2, 0, nameFields.length);
        nameFields = n2;

        TextField[] b2 = new TextField[n];
        System.arraycopy(breedFields, 0, b2, 0, breedFields.length);
        breedFields = b2;

        TextField[] a2 = new TextField[n];
        System.arraycopy(ageFields, 0, a2, 0, ageFields.length);
        ageFields = a2;
    }

    /** Shrink the per-row arrays by removing index 'idx'. */
    private void shrinkArraysByOne(int idx) {
        int n = editingRows.length - 1;
        boolean[] e2 = new boolean[n];
        for (int i = 0, j = 0; i < editingRows.length; i++) if (i != idx) e2[j++] = editingRows[i];
        editingRows = e2;

        TextField[] n2 = new TextField[n];
        for (int i = 0, j = 0; i < nameFields.length; i++) if (i != idx) n2[j++] = nameFields[i];
        nameFields = n2;

        TextField[] b2 = new TextField[n];
        for (int i = 0, j = 0; i < breedFields.length; i++) if (i != idx) b2[j++] = breedFields[i];
        breedFields = b2;

        TextField[] a2 = new TextField[n];
        for (int i = 0, j = 0; i < ageFields.length; i++) if (i != idx) a2[j++] = ageFields[i];
        ageFields = a2;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
