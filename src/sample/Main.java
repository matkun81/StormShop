package sample;

import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;


public class Main extends Application {
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    ListView<Good> lvProduct;
    ListView<Good> lvBasket;
    ObservableList<Good> productList;
    ObservableList<Good> bascketList;
    Button btn;
    Button addToMarket;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Stage stage = new Stage();
        primaryStage.setTitle("Магазин");
        FlowPane root = new FlowPane(10, 10);
        root.setAlignment(Pos.CENTER);
        primaryStage.setScene(new Scene(root, 300, 250));
        Label nameProduct = new Label("Выберите продукт");
        btn = new Button("Добавить в корзину");
        addToMarket = new Button("Добавить в магазин");
        Button sale = new Button("Купить");
        root.getChildren().addAll(addToMarket,btn,nameProduct);
        bascketList = FXCollections.observableArrayList();
        lvBasket = new ListView<>(bascketList);
        lvBasket.setPrefSize(80, 80);

        String url = "http://localhost:4567/getAll"; // подключаюсь к серверу
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        List<Good> goods = mapper.readValue(response.toString(), new TypeReference<List<Good>>() {
        });

            productList = FXCollections.observableArrayList(goods);
            lvProduct = new ListView<>(productList);
            lvProduct.setPrefSize(500, 200);
            root.getChildren().add(lvProduct);

        btn.setOnAction(event -> {
            Good sa = new Good(lvProduct.getSelectionModel().getSelectedItem().name,lvProduct.getSelectionModel().getSelectedItem().price,1);
            boolean wasAdded = true;
            if (lvProduct.getSelectionModel().getSelectedItem().count<1){
                throw new IllegalArgumentException("Нету товара на складе");
            } else {
                lvProduct.getSelectionModel().getSelectedItem().count--;
                lvProduct.refresh();
            }
            lvBasket.refresh();
            for(Good it:lvBasket.getItems()){
                if(it.name.equals(lvProduct.getSelectionModel().getSelectedItem().name)){
                    it.count++;
                    wasAdded = false;
                }
           }
           if (wasAdded) {
               bascketList.add(sa);
           }
       });
        sale.setOnAction(event -> {
            String postUrl = "http://localhost:4567/buyGoods";// put in your url
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost post  = new HttpPost(postUrl);
            StringEntity postingString = null;//gson.tojson() converts your pojo to json
            try {
                postingString = new StringEntity(GSON.toJson(lvBasket.getItems().toString()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");
            try {
                HttpResponse response1 = httpClient.execute(post);

            } catch (IOException e) {
                e.printStackTrace();
            }
            //String json = GSON.toJson(new Good(textField1.toString(),Integer.parseInt(textField2.toString()),Integer.parseInt(textField3.toString())));
        });
        addToMarket.setOnAction(event -> {
            FlowPane lastroot = new FlowPane(10, 10);
            stage.setScene(new Scene(lastroot, 300, 250));
            ListView <Good> lvProductShop = new ListView<>(productList);
            lastroot.getChildren().add(lvProductShop);
            TextField textField1 = new TextField();
            textField1.setPromptText("Введите название продукта");
            TextField textField2 = new TextField();
            textField2.setPromptText("Введите цену продукта");
            TextField textField3 = new TextField();
            textField3.setPromptText("Введите количество товара");
            lvProductShop.setOnMouseClicked((EventHandler<MouseEvent>) click -> {
                if (click.getClickCount() == 2) {
                    textField1.setText(lvProductShop.getSelectionModel().getSelectedItem().name);
                    textField2.setText(String.valueOf(lvProductShop.getSelectionModel().getSelectedItem().price));
                    textField3.setText(String.valueOf(lvProductShop.getSelectionModel().getSelectedItem().count));
                }
            });
            Button butAddtoShop = new Button("Добавить в магазин");
            Button deleteOfShop = new Button("Удалить из магазина");
            Button sendToServer = new Button("Отправить на сервер");
            lastroot.getChildren().addAll(textField1,textField2,textField3,butAddtoShop,deleteOfShop,sendToServer);
            butAddtoShop.setOnAction(event1 ->{
                Good s = new Good(textField1.getText(),Integer.parseInt(textField2.getText()),Integer.parseInt(textField3.getText()));
                boolean wasAdded = true;
                lvProductShop.refresh();
                for(Good it:lvProductShop.getItems()){
                    if(it.name.equals(textField1.getText())){
                        it.count= Integer.parseInt(textField3.getText());
                        it.price = Integer.parseInt(textField2.getText());
                        wasAdded = false;
                    }
                }
                if (wasAdded) {
                    productList.add(s);
                }
            });
            deleteOfShop.setOnAction(event3 ->{
                lvProductShop.refresh();
                for (Good it : productList) {
                    if (it.name.equals(lvProductShop.getSelectionModel().getSelectedItem().name)){
                        productList.remove(it);
                    }
                }

            });
            sendToServer.setOnAction(event2 -> {
                String postUrl = "http://localhost:4567/addGoods";// put in your url
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost post  = new HttpPost(postUrl);
                StringEntity postingString = null;//gson.tojson() converts your pojo to json
                try {
                    postingString = new StringEntity(GSON.toJson(productList));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                post.setEntity(postingString);
                post.setHeader("Content-type", "application/json");
                try {
                    HttpResponse response1 = httpClient.execute(post);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                //String json = GSON.toJson(new Good(textField1.toString(),Integer.parseInt(textField2.toString()),Integer.parseInt(textField3.toString())));
            });
            stage.show();
        });
        root.getChildren().addAll(lvBasket,sale);
        primaryStage.show();
    }
//    private void comparable(){
//
//        lvBasket.getItems().forEach(it -> {
//            if (Objects.equals(it.name, lvProduct.getSelectionModel().getSelectedItem().name)) {
//
//            }
//        });
    }
