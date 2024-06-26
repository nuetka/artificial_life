package org.example;


import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Boid {
    Vector position;
    Vector velocity;
    Vector acceleration;
   double health ;
    boolean dead = false;
    int age;
    int[] dna = new int[4];

    static int size = 3;
    static Path2D shape = new Path2D.Double();
    static {
        shape.moveTo(0,-size*2);
        shape.lineTo(-size, size*2);
        shape.lineTo(size,size*2);
        shape.closePath();
    }


    public Boid(boolean child, int[] parentDNA, Vector parentPosition, Vector vel) {
        if(child){
            this.position=new Vector(parentPosition.getXValue(),parentPosition.getYValue());
            Vector vv=new Vector(vel.getXValue(),vel.getYValue());
            vv.multiply(-1);
            this.velocity= vv;
        }else {
            this.position = new Vector((Math.random() * BoidRunner.WIDTH), (Math.random() * BoidRunner.HEIGHT));
        }
        double angle = Math.random()*360;
        double radius = Math.random()*2+2; //2-4 скорость
        this.velocity = new Vector((radius * Math.cos(angle)), (radius * Math.sin(angle)));
        this.acceleration = new Vector(0,0);
        this.health = 2;
        this.age = 60; // минута
        //this.dna = new ArrayList<>((int)(Math.random() * 4 - 2),(int)(Math.random() * 4 - 2));//от -2 до 2 от 0 до 100
        if(!child) {
            do {
                this.dna[0] = (int) (Math.random() * 5 - 2);// goodfood weight от -2 до 3
            } while (this.dna[0] == 0);
            do {
                this.dna[1] = (int) (Math.random() * 5 - 2);// плохая еда яд от -2 до 3
            } while (this.dna[1] == 0);


            do {
                this.dna[2] = (int) (Math.random() * 100);
            } while (this.dna[2] == 0);

            do {
                this.dna[3] = (int) (Math.random() * 100);
            } while (this.dna[3] == 0);
        }
        else{
            int value;
            this.dna[0]=parentDNA[0];
            this.dna[1]=parentDNA[1];
            this.dna[2]=parentDNA[2];
            this.dna[3]=parentDNA[3];

                if (Math.random() <= BoidRunner.mutation_rate) {
                    do {
                         value = (int) (Math.random() * (2*BoidRunner.food_attract)-BoidRunner.food_attract);
                        this.dna[0]+=value;
                        if( this.dna[0]>5){
                            this.dna[0]=5;
                        }
                        else if (this.dna[0]<-5){
                            this.dna[0]=-5;
                        }
                    } while (value == 0);

            }
            if (Math.random() < BoidRunner.mutation_rate) {
                do {
                    value = (int) (Math.random() * (2*BoidRunner.poison_attract)-BoidRunner.poison_attract);
                    this.dna[1]+=value;
                    if( this.dna[1]>5){
                        this.dna[1]=5;
                    }
                    else if (this.dna[1]<-5){
                        this.dna[1]=-5;
                    }
                } while (value == 0);


            }
            if (Math.random() < BoidRunner.mutation_rate) {
                do {
                    value = (int) (Math.random() * (2*BoidRunner.food_percept)-BoidRunner.food_percept);
                    this.dna[2]+=value;
                    if( this.dna[2]>200){
                        this.dna[2]=200;
                    }
                    else if (this.dna[2]<-200){
                        this.dna[2]=-200;
                    }
                } while (value == 0);


            }
            if (Math.random() < BoidRunner.mutation_rate) {
                do {
                    value = (int) (Math.random() * (2*BoidRunner.poison_percept)-BoidRunner.poison_percept);
                    this.dna[3]+=value;
                    if( this.dna[3]>200){
                        this.dna[3]=200;
                    }
                    else if (this.dna[2]<-200){
                        this.dna[3]=-200;
                    }
                } while (value == 0);


            }



        }

    }

    public Boid(int x, int y) {

        this.position = new Vector(x,y);

        double angle = Math.random()*360;
        double radius = Math.random()*2+2; //2-4 скорость
        this.velocity = new Vector((radius * Math.cos(angle)), (radius * Math.sin(angle)));
        this.acceleration = new Vector(0,0);
        this.health = 2;
        this.age = 60; // минута

            do {
                this.dna[0] = (int) (Math.random() * 5 - 2);// goodfood weight от -2 до 3
            } while (this.dna[0] == 0);
            do {
                this.dna[1] = (int) (Math.random() * 5 - 2);// плохая еда яд от -2 до 3
            } while (this.dna[1] == 0);


            do {
                this.dna[2] = (int) (Math.random() * 100);
            } while (this.dna[2] == 0);

            do {
                this.dna[3] = (int) (Math.random() * 100);
            } while (this.dna[3] == 0);
        }




    Boid getChild(){

        return new Boid(true,this.dna,this.position,this.velocity);
    }
    boolean dead(){
        return (this.health<0);
    }



//    void behaviors(ArrayList<Food> good,ArrayList<Food> bad){
//        //Vector attraction = this.eat(good,BoidRunner.food_value,this.dna[2]);// направление до еды
//         Vector repulsion = this.eat(bad, BoidRunner.poison_value, this.dna[3]);// направление до яда
//
//        // Scale the steering forces via. DNA
//        //attraction.multiply(this.dna[0]);
//       repulsion.multiply(this.dna[1]);
//
////        if (((attraction.getMagnitude() + repulsion.getMagnitude()) != 0) ) {
//            //this.acceleration.add(attraction);// We could add mass here if we want A = F / M
//          this.acceleration.add(repulsion);
////        } else {
////            this.wander(); // Вызываем метод wander, если еда не найдена
//       // надо если в радиусе восприятия нет нужного
////        }


    //}
    void behaviors(ArrayList<Food> good, ArrayList<Food> bad) {
        // Create a thread for attraction force calculation
        Thread attractionThread = new Thread(() -> {
            Vector attraction = this.eat(good, BoidRunner.food_value, this.dna[2]);
            attraction.multiply(this.dna[0]);
            synchronized (this) {
                if (attraction.getMagnitude() != 0) {
                    this.acceleration.add(attraction);
                }
            }
        });

        // Create a thread for repulsion force calculation
        Thread repulsionThread = new Thread(() -> {
            Vector repulsion = this.eat(bad, BoidRunner.poison_value, this.dna[3]);
            repulsion.multiply(this.dna[1]);
            synchronized (this) {
                if (repulsion.getMagnitude() != 0) {
                    this.acceleration.add(repulsion);
                }
            }
        });

        // Start both threads
        attractionThread.start();
        repulsionThread.start();

        // Wait for both threads to finish
        try {
            attractionThread.join();
            repulsionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Continue with other behaviors...
    }


    Vector eat(ArrayList<Food> targets,double nutrition, double perception){
        double record = Double.MAX_VALUE;
        Food closest = null;
        //boolean needToRemove=false;

        for (int i = targets.size()-1;i>=0;i--){
            double d = this.position.dist(targets.get(i).position);
            //момент съедания
            if(d<5){// или max speed ипа прыг

                double value;
                //уничтожение еды
                targets.remove(i);

                value=this.health;
                value+=nutrition;
                this.health=BoidRunner.round(value,2);

            }

            //поиск ближайшей еды которая может быть воспринята
            else if (d < record && d < perception) {
                record = d;
                closest = targets.get(i);
            }

        }

        if (closest != null) {
            // Steer towards that target
            return this.seek(closest.position);
        }

        // Target not found, etc.
        return new Vector(0, 0);

    }



    Vector seek(Vector target){

        Vector desired = new Vector(target.sub(this.position)[0],target.sub(this.position)[1]);

        desired.setMagnitude(BoidRunner.maxSpeed);
        // Steering = Desired minus velocity
        Vector steer = new Vector(desired.sub(this.velocity)[0],desired.sub(this.velocity)[1]);
        steer.limit(BoidRunner.max_force);
        //this.acceleration.add(steer);

        return steer;
    }

    void update() {

        this.velocity.add(this.acceleration);
        this.velocity.limit(BoidRunner.maxSpeed);
        this.position.add(this.velocity);
        // Reset accelerationelertion to 0 each cycle
        // this.acceleration.multiply(0);
    }

    void edges() {
        if(this.position.xvalue > BoidRunner.WIDTH)
            this.position.xvalue = 0;
        else if(this.position.xvalue < 0)
            this.position.xvalue = BoidRunner.WIDTH;

        if(this.position.yvalue > BoidRunner.HEIGHT)
            this.position.yvalue = 0;
        else if(this.position.yvalue < 0)
            this.position.yvalue = BoidRunner.HEIGHT;
    }

//    void wander() {
//        // Создаем вектор с случайным углом направления
//        double angle = Math.random() * 2 * Math.PI;
//        Vector randomForce = new Vector(Math.cos(angle), Math.sin(angle));
//        randomForce.multiply(BoidRunner.max_force); // Умножаем на максимальную силу, чтобы получить изменение ускорения
//        this.acceleration.add(randomForce); // Добавляем случайное ускорение к текущему ускорению
//    }

    public void draw(Graphics2D g) {
        AffineTransform save = g.getTransform();
        g.translate((int)this.position.xvalue, (int)this.position.yvalue);
            g.rotate(this.velocity.dir() + Math.PI/2);

        // Рисуем радиус восприятия хорошей еды
        g.setColor(Color.GREEN);
        g.drawOval(-dna[2], -dna[2], dna[2]*2, dna[2]*2);
       // g.drawOval(-200, -200, 200*2, 200*2);

        g.drawLine(-1, -1, -1, -dna[0]*100); // Линия привлекательности хорошей еды
       // g.drawLine(-1, -1, -1, -500); // Линия привлекательности хорошей еды

        //g.drawLine(0, 0, 0, -100); // Линия привлекательности хорошей еды

        // Рисуем радиус восприятия яда
        g.setColor(Color.RED);
        g.drawOval(-dna[3], -dna[3], dna[3]*2, dna[3]*2);

        // Рисуем линии привлекательности
          g.drawLine(1, 1, 1, -dna[1]*100); // Линия привлекательности яда
        //g.drawLine(0, 0, 0, -100); // Линия привлекательности яда

        g.setColor(Color.WHITE);
        g.fill(shape);
        g.draw(shape);




        g.setTransform(save);
        // Рисуем здоровье справа от Boid
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(this.health), (int)this.position.xvalue + 20, (int)this.position.yvalue);
    }

}