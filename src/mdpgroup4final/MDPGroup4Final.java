package mdpgroup4final;

import algorithm.MazeExplorer;
import controllers.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;
import static javafx.application.Application.launch;

import models.Arena;
import utilities.Orientation;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MDPGroup4Final extends Application {
    private static MDPGroup4Final instance;
    private Controller controller;

    private final int SCENE_WIDTH = 800;
    private final int SCENE_HEIGHT = 550;
    private final String SCENE_TITLE = "MAZE";
    private final int CANVAS_WIDTH = 300;
    private final int CANVAS_HEIGHT = 400;
    private final int DRAW_CANVAS_X	= 10;
    private final int CANVAS_Y  = 10;
    private final int MARGIN = 20;
    private final int BUTTON_WIDTH = 100;
    private final int BUTTON_HEIGHT = 30;

    private final int BUTTON_SIDE_Y = CANVAS_Y;
    private final int BUTTON_SIDE_X = DRAW_CANVAS_X+CANVAS_WIDTH+MARGIN;
    private final int BUTTON_BOTTOM_X = DRAW_CANVAS_X;
    private final int BUTTON_BOTTOM_Y = CANVAS_Y+CANVAS_HEIGHT+MARGIN;
    private final int ROBOT_CANVAS_X	= BUTTON_SIDE_X+BUTTON_WIDTH+MARGIN;
    private final int TEXTFIELD_WIDTH = 100;
    private final int TEXTFIELD_HEIGHT = 25;
    private final int BOTTOM_LABEL_X = ROBOT_CANVAS_X;
    private final int BOTTOM_LABEL_WIDTH = 130;
    private final int TEXTFIELD_X = ROBOT_CANVAS_X+BOTTOM_LABEL_WIDTH;
    private final int TEXTFIELD_Y = BUTTON_BOTTOM_Y;
    private final int LABEL_TIMER_WIDTH = 50;
    private final int SLEEP_DURATION    = 10;
    private final int TIMER_UPDATE_DURATION = 100;
    private final String[] SIDE_BUTTON_TEXT = {"Obstacle","Path","Start","Goal","Robot"};
    private final String[] BOTTOM_BUTTON_TEXT = {"Save Map","Load Map","Explore"};

    private final Color COLOR_GOAL = Color.GREEN;
    private final Color COLOR_START = Color.RED;
    private final Color COLOR_PATH = Color.LIGHTGRAY;
    private final Color COLOR_OBSTACLE = Color.GRAY;
    private final Color COLOR_ROBOT = Color.YELLOW;
    private final Color COLOR_ROBOT_FACE = Color.RED;
    private final Color COLOR_GRID = Color.BLACK;
    private final Color COLOR_EXPLORED = Color.LIGHTPINK;

    private final int PATH = 0;
    private final int OBSTACLE = 1;
    private final int START = 2;
    private final int GOAL = 3;
    private final int ROBOT = 4;

    private final Color[] COLOR_REF = {
            COLOR_PATH,COLOR_OBSTACLE,COLOR_START,COLOR_GOAL,COLOR_ROBOT,COLOR_EXPLORED
            //COLOR_EXPLORED
    };
    private final int CELL_SIZE = CANVAS_WIDTH/Arena.COL;//assume cell is square
    private GraphicsContext draw_gc; //Graphic Context of Canvas
    private GraphicsContext robot_gc; //Graphic Context of Canvas
    private ComboBox<String> ddl;
    private ComboBox<Integer> speed_ddl;
    private Label text_timer;
    private TextField timelimit_input;
    private TextField coveragelimit_input;
    private int draw_mode = OBSTACLE;
    private int[][] robot_loc_array;
    private int clicked_row,clicked_col;//for robot drag effect
    private final int DRAG_THRESHOLD = 3;

    private Timer ui_timer;
    private Timer robot_timer;
    private Timer timer_timer;

    @Override
    public void start(Stage primaryStage) throws Exception{
        instance = this;
        controller = Controller.getInstance();
        primaryStage.setTitle(SCENE_TITLE);
        primaryStage.setScene(new Scene(createGroup(), SCENE_WIDTH,SCENE_HEIGHT,Color.LIGHTGRAY));
        primaryStage.show();
        primaryStage.setOnCloseRequest((event)->{
            System.exit(0);
        });
    }

    public static MDPGroup4Final getInstance(){
        return instance;
    }

    private Group createGroup(){
        Group g = new Group();
        g.getChildren().add(createDrawCanvas());
        g.getChildren().add(createRobotCanvas());
        for(Button b:createMazeSetupButtons())
            g.getChildren().add(b);
        for(Button b:createBottomButtons())
            g.getChildren().add(b);
        g.getChildren().add(getDropdownlist());
        g.getChildren().add(getSpeedDropdownlist());
        g.getChildren().add(getTextTimer());
        g.getChildren().add(createTextLabel());

        g.getChildren().add(getTimeLimitInput());
        g.getChildren().add(getCoverageLimitInput());
        g.getChildren().add(createTimeLimitLabel());
        g.getChildren().add(createCoverageLimitLabel());
        g.getChildren().add(createCoveragePercentageLabel());
        g.getChildren().add(createLimitButton());
        return g;
    }

    private ComboBox<String> getDropdownlist(){
        if(ddl==null){
            String[] list = {"Simutation","Real"};
            ddl = new ComboBox<String>();
            ddl.getItems().addAll(list);
            ddl.setPrefSize(BUTTON_WIDTH*2,BUTTON_HEIGHT);
            ddl.setLayoutX(DRAW_CANVAS_X);
            ddl.setLayoutY(BUTTON_BOTTOM_Y+2*BUTTON_HEIGHT);
            ddl.setValue(list[0]);

            EventHandler handler = (event)->{
                Controller.isRealRun = ddl.getSelectionModel().getSelectedItem().equals(list[1]);
            };
            ddl.setOnAction(handler);
        }
        return ddl;
    }

    private ComboBox<Integer> getSpeedDropdownlist(){
        if(speed_ddl==null){
            Integer[] list = {1,2,5,10,20,25,50};
            speed_ddl = new ComboBox<Integer>();
            speed_ddl.getItems().addAll(list);
            speed_ddl.setPrefSize(BUTTON_WIDTH,BUTTON_HEIGHT);
            speed_ddl.setLayoutX(BUTTON_SIDE_X);
            speed_ddl.setLayoutY(BUTTON_SIDE_Y+SIDE_BUTTON_TEXT.length*BUTTON_HEIGHT);
            speed_ddl.setValue(list[0]);

            EventHandler handler = (event)->{
                controller.setSimulationSpeed(speed_ddl.getSelectionModel().getSelectedItem());
            };
            controller.setSimulationSpeed(speed_ddl.getSelectionModel().getSelectedItem());
            speed_ddl.setOnAction(handler);
        }
        return speed_ddl;
    }

    private Label createTextLabel(){
        Label label = new Label("Timer  : ");
        label.setLayoutX(DRAW_CANVAS_X+5);
        label.setLayoutY(getDropdownlist().getLayoutY()+BUTTON_HEIGHT);
        label.setPrefSize(LABEL_TIMER_WIDTH,BUTTON_HEIGHT);
        return label;
    }
    private Label getTextTimer(){
        if(text_timer == null){
            text_timer = new Label("0 ");
            text_timer.setPrefSize(BUTTON_WIDTH,BUTTON_HEIGHT);
            text_timer.setLayoutX(DRAW_CANVAS_X+LABEL_TIMER_WIDTH+5);
            text_timer.setLayoutY(getDropdownlist().getLayoutY()+BUTTON_HEIGHT);
        }
        return text_timer;
    }

    private Canvas createDrawCanvas(){
        Canvas c = new Canvas(CANVAS_WIDTH,CANVAS_HEIGHT);
        c.setLayoutX(DRAW_CANVAS_X);
        c.setLayoutY(CANVAS_Y);
        EventHandler handler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int row = (int) (event.getY() / CELL_SIZE);
                int col = (int) (event.getX() / CELL_SIZE);
                if (draw_mode == PATH){
                    controller.setFree(row,col);
                    if(controller.isInStartGoalArea(row,col)==0)
                        drawGrid(draw_gc,row,col,COLOR_PATH);
                }
                else if(draw_mode == OBSTACLE){
                    if(controller.setObstacle(row,col))
                        drawGrid(draw_gc, row, col,COLOR_OBSTACLE);
                }
                else if(draw_mode == ROBOT){
                    if(event.getEventType()==MouseEvent.MOUSE_DRAGGED){
                        int different_row = row - clicked_row;
                        int different_col = col - clicked_col;
                        if(different_row<-DRAG_THRESHOLD)
                            controller.setRobotOrientation(Orientation.NORTH);
                        else if(different_row>DRAG_THRESHOLD)
                            controller.setRobotOrientation(Orientation.SOUTH);
                        else if(different_col>DRAG_THRESHOLD)
                            controller.setRobotOrientation(Orientation.EAST);
                        else if(different_col<-DRAG_THRESHOLD)
                            controller.setRobotOrientation(Orientation.WEST);

                        drawRobot(draw_gc);

                    }
                    else {
                        if(!event.isStillSincePress())return;
                        clicked_col = col;clicked_row = row;
                        for (int[] i : controller.getRobotLocation()) {
                            int in_goal_start = controller.isInStartGoalArea(i[0], i[1]);
                            if (in_goal_start == 0) drawGrid(draw_gc, i[0], i[1], COLOR_PATH);
                            else drawGrid(draw_gc, i[0], i[1], in_goal_start == 1 ? COLOR_START : COLOR_GOAL);
                        }
                        int loc[] = {row, col};
                        controller.setRobotLocation(loc);
                        drawRobot(draw_gc);
                    }
                }
                else{
                    //remove previous
                    for(int i:controller.getStartGoalLoc(draw_mode==START)){
                        int updated_row = i/Arena.COL;
                        int updated_col = i%Arena.COL;
                        controller.setFree(updated_row,updated_col);
                        drawGrid(draw_gc,updated_row,updated_col,COLOR_PATH);
                    }
                    //set
                    controller.setStartGoal(draw_mode==START,row,col);
                    for(int i:controller.getStartGoalLoc(draw_mode==START)){
                        int updated_row = i/Arena.COL;
                        int updated_col = i%Arena.COL;
                        drawGrid(draw_gc,updated_row,updated_col,draw_mode==START?COLOR_START:COLOR_GOAL);
                    }
                }
            }
        };

        c.setOnMouseClicked(handler);
        c.setOnMouseDragged(handler);
        draw_gc = c.getGraphicsContext2D();

        //initialize Maze canvas
        drawMaze(draw_gc);

        //start goal
        drawStartGoal(draw_gc);

        return c;
    }

    private Canvas createRobotCanvas(){
        Canvas c = new Canvas(CANVAS_WIDTH,CANVAS_HEIGHT);
        c.setLayoutX(ROBOT_CANVAS_X);
        c.setLayoutY(CANVAS_Y);
        robot_gc = c.getGraphicsContext2D();

        //initialize Maze canvas
        drawMaze(robot_gc);
        //start goal
        return c;
    }

    private void drawMaze(GraphicsContext gc){
        boolean[][] map_data = controller.getArenaInformation();
        for(int i=0;i<map_data.length;i++)
            for(int j=0;j<map_data[i].length;j++)
                drawGrid(gc,i,j,map_data[i][j]?COLOR_OBSTACLE:COLOR_PATH);
    }

    private void drawStartGoal(GraphicsContext gc){
        for(int j=0;j<2;j++)
            for(int i:controller.getStartGoalLoc(j%2==0)){
                int row = i/Arena.COL;
                int col = i%Arena.COL;
                drawGrid(gc,row,col,j%2==0?COLOR_START:COLOR_GOAL);
            }
    }

    private void drawGrid(GraphicsContext gc,int row,int col,Color color){
        int a = col*CELL_SIZE;
        int c = row*CELL_SIZE;

        //grid
        gc.setFill(COLOR_GRID);
        gc.fillRect(a,c,CELL_SIZE,CELL_SIZE);
        //maze
        gc.setFill(color);
        gc.fillRect(a+1,c+1,CELL_SIZE-2,CELL_SIZE-2);
    }

    //Create all side buttons
    private Button[] createMazeSetupButtons(){
        Color [] c = {Color.BLACK,COLOR_OBSTACLE,COLOR_START,COLOR_GOAL,Color.GOLDENROD};
        Button[] buttons = new Button[SIDE_BUTTON_TEXT.length];
        EventHandler handler = (event)->{
            String object_string = event.getSource().toString();
            if(object_string.contains(SIDE_BUTTON_TEXT[0]))
                draw_mode = OBSTACLE;
            else if(object_string.contains(SIDE_BUTTON_TEXT[1]))
                draw_mode = PATH;
            else if(object_string.contains(SIDE_BUTTON_TEXT[2]))
                draw_mode = START;
            else if(object_string.contains(SIDE_BUTTON_TEXT[3]))
                draw_mode = GOAL;
            else if(object_string.contains(SIDE_BUTTON_TEXT[4]))
                draw_mode = ROBOT;
            else
                draw_mode = PATH;
        };
        //insert button
        for(int i=0;i<SIDE_BUTTON_TEXT.length;i++)
            buttons[i] = createButton(SIDE_BUTTON_TEXT[i],BUTTON_SIDE_X,BUTTON_SIDE_Y+i*BUTTON_HEIGHT,c[i],handler);
        return buttons;
    }


    //Create bottom buttons
    private Button[] createBottomButtons(){
        Button[] buttons = new Button[BOTTOM_BUTTON_TEXT.length];
        EventHandler handler = (event)-> {
            if(event.getTarget().toString().contains(BOTTOM_BUTTON_TEXT[0])){
                //Save map
                try{
                    controller.saveMap();
                    System.out.println("Map Saved");
                }
                catch(Exception ex){
                    //Create dialog
                    ex.printStackTrace();
                }

            }
            else if(event.getTarget().toString().contains(BOTTOM_BUTTON_TEXT[1])){
                //Load map
                try{
                    controller.loadMap();
                    drawMaze(draw_gc);
                    drawStartGoal(draw_gc);
                    System.out.println("Map Loaded");
                }
                catch(Exception ex){
                    //Create dialog
                    ex.printStackTrace();
                }
            }
            else if(event.getTarget().toString().contains(BOTTOM_BUTTON_TEXT[2])){
                drawRobot(robot_gc);
                ui_timer = new Timer();
                ui_timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(controller.needUpdate()) {
                            if(controller.getPrevious()!=null) {
                                for (int i = 0; i < controller.getPrevious().length; i++) {
                                    //if (controller.getPrevious()[i][0] < 0)//Shoult noe happen || controller.getPrevious()[i][0] >= Arena.ROW)
                                    //   break;
                                    drawGrid(robot_gc, controller.getPrevious()[i][0], controller.getPrevious()[i][1], COLOR_EXPLORED);
                                }
                            }

                            //update detected
//                            for(int i = 0;i<controller.getDetected().length;i++) {
//                                int offset_row = 0, offset_col = 0;
//                                int[] detected = controller.getDetected()[i];
//                                if(detected[Controller.DISTANCE]== MazeExplorer.IGNORE_DISTANCE)continue;
//                                switch (detected[Controller.ABSOLUTE_ORIENTATION]) {
//                                    case Orientation.NORTH:
//                                        offset_row = -1;
//                                        break;
//                                    case Orientation.SOUTH:
//                                        offset_row = +1;
//                                        break;
//                                    case Orientation.WEST:
//                                        offset_col = -1;
//                                        break;
//                                    case Orientation.EAST:
//                                        offset_col = 1;
//                                        break;
//                                }
//                                int distance;
//                                if(detected[Controller.DISTANCE]==-1)distance = detected[Controller.DETECT_RANGE]+1;
//                                else{
//                                    //if detected obstalce
//                                    distance = detected[Controller.DISTANCE];
//                                    drawGrid(robot_gc, detected[Controller.ABSOLUTE_ROW] + offset_row * distance, detected[Controller.ABSOLUTE_COL] + offset_col * distance, COLOR_OBSTACLE);
//                                }
//                                for (int j = 1; j < distance; j++)
//                                    drawGrid(robot_gc, detected[Controller.ABSOLUTE_ROW] + offset_row * j, detected[Controller.ABSOLUTE_COL] + offset_col * j, COLOR_EXPLORED);
//                            }
                            int[][][] data = controller.getPerceivedMapData();
                            for(int i=0;i<data.length;i++){
                                for(int j=0;j<data[i].length;j++){
                                    int[] target = data[i][j];
                                    if(target[Controller.PERCEIVE_DAT]==Controller.UNKNOWN)continue;
                                    drawGrid(robot_gc, target[Controller.PERCEIVE_ROW],target[Controller.PERCEIVE_COL],target[Controller.PERCEIVE_DAT] == Controller.PATH?COLOR_EXPLORED:COLOR_OBSTACLE);
                                }
                            }
                            drawRobot(robot_gc);
                            controller.updated();
                        }
                        if(controller.isDone()){
                            ui_timer.cancel();
                            timer_timer.cancel();
                            //add in stop for timer
                        }
                    }
                },0,SLEEP_DURATION);

                robot_timer = new Timer();
                robot_timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        controller.startRobot();
                    }
                },SLEEP_DURATION);


            }
        };
        //insert button
        for(int i=0;i<BOTTOM_BUTTON_TEXT.length;i++)
            buttons[i] = createButton(BOTTOM_BUTTON_TEXT[i],BUTTON_BOTTOM_X+(i%2)*BUTTON_WIDTH,BUTTON_BOTTOM_Y+i/2*BUTTON_HEIGHT,null,handler);
        return buttons;
    }


    public void startTimer(){
        timer_timer = new Timer();//for update label
        timer_timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                controller.updateTime(TIMER_UPDATE_DURATION);
                Platform.runLater(()->{
                    getTextTimer().setText(Long.toString(controller.getTime()));
                });
            }
        },0,TIMER_UPDATE_DURATION);
    }

    public void stopTimer(){
        if(timer_timer!=null){
            timer_timer.cancel();
        }
    }

    private Button createButton(String text, double x, double y, Color c,EventHandler listener){
        Button button = new Button(text);
        button.setLayoutX(x);
        button.setLayoutY(y);
        if(c!=null)button.setTextFill(c);
        button.setPrefSize(BUTTON_WIDTH,BUTTON_HEIGHT);
        button.setOnAction(listener);
        return button;
    }

    private TextField getTimeLimitInput(){
        if(timelimit_input == null) {
            timelimit_input = new TextField(Integer.toString(controller.getTimeLimit()));
            timelimit_input.setLayoutX(TEXTFIELD_X);
            timelimit_input.setLayoutY(TEXTFIELD_Y);
            timelimit_input.setPrefSize(TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
        }
        return timelimit_input;
    }

    private TextField getCoverageLimitInput(){
        if(coveragelimit_input == null) {
            coveragelimit_input = new TextField(Integer.toString(controller.getCoverageLimit()));
            coveragelimit_input.setLayoutX(TEXTFIELD_X);
            coveragelimit_input.setLayoutY(TEXTFIELD_Y+TEXTFIELD_HEIGHT);
            coveragelimit_input.setPrefSize(TEXTFIELD_WIDTH-20, TEXTFIELD_HEIGHT);
        }
        return coveragelimit_input;
    }
    private Label createTimeLimitLabel(){
        Label timer_label = new Label("Time Limit (Seconds) : ");
        timer_label.setLayoutX(BOTTOM_LABEL_X);
        timer_label.setLayoutY(getTimeLimitInput().getLayoutY());
        timer_label.setPrefSize(BOTTOM_LABEL_WIDTH, TEXTFIELD_HEIGHT);
        return timer_label;
    }

    private Label createCoverageLimitLabel(){
        Label coverage_label = new Label("Coverage Limit : ");
        coverage_label.setLayoutX(BOTTOM_LABEL_X);
        coverage_label.setLayoutY(getCoverageLimitInput().getLayoutY());
        coverage_label.setPrefSize(BOTTOM_LABEL_WIDTH, TEXTFIELD_HEIGHT);
        return coverage_label;
    }

    private Label createCoveragePercentageLabel(){
        Label coverage_percentage_label = new Label("%");
        coverage_percentage_label.setLayoutX(getCoverageLimitInput().getLayoutX()+getCoverageLimitInput().getPrefWidth()+5);
        coverage_percentage_label.setLayoutY(getCoverageLimitInput().getLayoutY());
        coverage_percentage_label.setPrefSize(20, TEXTFIELD_HEIGHT);
        return coverage_percentage_label;
    }

    //Create all side buttons
    private Button createLimitButton(){
        Button button;
        EventHandler handler = (event)->{
            if(controller.setCoverageLimit(coveragelimit_input.getText())){
                System.err.println("ERROR INPUT@CoverageLimit");
                coveragelimit_input.setText(Integer.toString(controller.getCoverageLimit()));
            }
            if(controller.setTimeLimit(timelimit_input.getText())){
                System.err.println("ERROR INPUT@TimeLimit");
                timelimit_input.setText(Integer.toString(controller.getTimeLimit()));
            }
        };
        //insert button
        button = createButton("Update",BOTTOM_LABEL_X,getCoverageLimitInput().getLayoutY()+getCoverageLimitInput().getPrefHeight(),null,handler);
        return button;
    }


    private void drawRobot(GraphicsContext g){
        int orientation = controller.getRobotOrientation1357();
        int[][] robot_loc = controller.getRobotLocation();
        for(int i=0;i<robot_loc.length;i++)
            drawGrid(g,robot_loc[i][0],robot_loc[i][1],i==orientation?COLOR_ROBOT_FACE:COLOR_ROBOT);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
