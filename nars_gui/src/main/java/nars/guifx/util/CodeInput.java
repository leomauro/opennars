package nars.guifx.util;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/2/15.
 */
public class CodeInput extends BorderPane {

    public CodeInput() {
        this("");
    }

    public CodeInput(String text) {
        super();

        //StyleClassedTextArea codeArea = new StyleClassedTextArea();
        //codeArea.getStyleClass().add("code-area");
        //codeArea.setStyle(0, Lists.newArrayList("code-area-default"));

        TextArea codeArea= new TextArea();

        codeArea.appendText(text);

        setCenter(codeArea);

        codeArea.setMinSize(100,100);

        //codeArea.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        codeArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        //codeArea.autosize();



        codeArea.setOnKeyPressed(k -> {
            //ctrl-enter
            if (k.isControlDown() && k.getCode() == KeyCode.ENTER) {
                runLater(()-> {
                    try {
                        if (onInput(codeArea.getText())) {
                            codeArea.clear();
                        }
                        setTop(null);
                        layout();
                    }
                    catch (Exception e) {
                        Label err = new Label("invalid Narsese, click to enter as natural language");
                        err.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                                    try {
                                        String s = e.getMessage();
                                        String[] spl = s.split(" ");
                                        String total = "(&/";
                                        for (String S : spl) {
                                            total += ",<\"" + S + "\" --> WORD>,/10";
                                        }
                                        total+="). :|:";
                                        if (onInput(total)) {
                                            codeArea.clear();
                                            setTop(null);
                                            layout();
                                        }
                                    }
                                    catch(Exception ex) {}
                                }
                            }
                        });
                        //Button err = new Button("not valid Narsese, click to enter as natural language"); //e.getMessage()
                        err.getStyleClass().add("error");
                        setTop(err);
                        layout();
                        //e.printStackTrace();
                        String s = e.getMessage();
                    }
                });
            }
        });
    }

    /** return false to indicate input was not accepted, leaving it as-is.
     * otherwise, return true that it was accepted and the buffer will be cleared. */
    public boolean onInput(String s) {
        return true;
    }
}
