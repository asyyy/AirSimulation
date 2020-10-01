
/* AirSimulation class
 *
 * TP of SE (version 2020)
 *
 * AM
 */

import java.sql.SQLOutput;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AirSimulation
{
   private int nAgent1;
   private int nAgent2;
   private int nAgent3;
   private int nAgent4;
   private Aircraft a;
   public final int nagents = 4;
   Semaphore sem2 = new Semaphore(1);

   // Constructor
   public AirSimulation()
   {
      this.nAgent1 = 0;
      this.nAgent2 = 0;
      this.nAgent3 = 0;
      this.nAgent4 = 0;
      this.a = new Aircraft();  // standard model
   }

   // Reference to Aircraft
   public Aircraft getAircraftRef()
   {
      return this.a;
   }

   // Agent1
   public void agent1() throws InterruptedException
   {
      boolean placed = false;
      Random R = new Random();
      ArrayList<Integer> emergRows = this.a.getEmergencyRowList();

      // generating a new Customer
      Customer c = new Customer();

      // randomly pick a seat
      do
      {
         int row = R.nextInt(this.a.getNumberOfRows());
         int col = R.nextInt(this.a.getSeatsPerRow());

         // verifying whether the seat is free
         if (this.a.isSeatEmpty(row,col))
         {
            // if this is an emergency exit seat, and c is over60, then we skip
            if (!emergRows.contains(row) || !c.isOver60() || this.a.numberOfFreeSeats() <= this.a.getSeatsPerRow() * this.a.getNumberEmergencyRows())
            {
               this.a.add(c,row,col);
               placed = true;
            }
         }
      }
      while (!placed && !this.a.isFlightFull());

      // updating counter
      if (placed)  this.nAgent1++;
   }

   // Agent2
   public void agent2() throws InterruptedException
   {
      boolean placed = false;
      ArrayList<Integer> emergRows = this.a.getEmergencyRowList();

      // generating a new Customer
      Customer c = new Customer();

      // searching free seats on the seatMap
      int row = 0;
      while (!placed && !this.a.isFlightFull() && row < this.a.getNumberOfRows())
      {
         int col = 0;
         while (!placed && col < this.a.getSeatsPerRow())
         {
            // verifying whether the seat is free
            if (this.a.isSeatEmpty(row,col))
            {
               // if this is an emergency exit seat, and c needs assistence, then we skip
               if (!emergRows.contains(row) || !c.needsAssistence() || this.a.numberOfFreeSeats() <= this.a.getSeatsPerRow() * this.a.getNumberEmergencyRows())
               {

                  this.a.add(c,row,col);
                  placed = true;
               }
            }
            col++;
         }
         row++;
      }

      // updating counter
      if (placed)  this.nAgent2++;
   }

   // Agent3
   public void agent3() throws InterruptedException
   {
      Random R = new Random();

      int row1;
      int col1;

      int row2;
      int col2;

      do {
         row1 = R.nextInt(this.a.getNumberOfRows());
         col1 = R.nextInt(this.a.getSeatsPerRow());

         row2 = R.nextInt(this.a.getNumberOfRows());
         col2 = R.nextInt(this.a.getSeatsPerRow());
      }
      while(this.a.isSeatEmpty(row2,col2) || this.a.isSeatEmpty(row1,col1));

      if(row1 > row2){
         if(this.a.getCustomer(row1,col1).getFlyerLevel() < this.a.getCustomer(row2,col2).getFlyerLevel()){
            swap(row1,col1,row2,col2);
         }
      }else{
         if(this.a.getCustomer(row1,col1).getFlyerLevel() > this.a.getCustomer(row2,col2).getFlyerLevel()){
            swap(row1,col1,row2,col2);
         }
      }

      this.nAgent3++;
   }



   /**
    * Echange la place de 2 passagers.
    *
    * @param row1 allée du passager n°1
    * @param col1 colonne du passager n°1
    * @param row2 allée du passager n°2
    * @param col2 colonne du passager n°2
    */
   private void swap(int row1,int col1,int row2,int col2) throws InterruptedException {
      Customer c1 = this.a.getCustomer(row1,col1);
      Customer c2 = this.a.getCustomer(row2,col2);

      this.a.freeSeat(row1,col1);
      this.a.freeSeat(row2,col2);

      this.a.add(c1,row2,col2);
      this.a.add(c2,row1,col1);

   }

   // Agent4: the virus
   public void agent4() throws InterruptedException
   {
      for (int i = 0; i < this.a.getNumberOfRows(); i++)
      {
         for (int j = 0; j < this.a.getSeatsPerRow(); j++)
         {
            Customer c = this.a.getCustomer(i,j);
            this.a.freeSeat(i,j);
            if (c != null) this.a.add(c,i,j);

         }
      }

      this.nAgent4++;
   }

   // Resetting
   public void reset()
   {
      this.nAgent1 = 0;
      this.nAgent2 = 0;
      this.nAgent3 = 0;
      this.nAgent4 = 0;
      this.a.reset();
   }
   public static class tAgent extends Thread{
      Semaphore sem;
      AirSimulation avion;
      int numAgent;
      public tAgent(Semaphore sem, AirSimulation avion, int i) {
         this.sem = sem;
         this.avion = avion;
         this.numAgent=i;
      }

      @Override
      public void run() {
         try {
            sem.acquire();
            switch (numAgent){
               case 1 : avion.agent1();
                  break;
               case 2 : avion.agent2();
                  break;
               case 3 : avion.agent3();
                  break;
               case 4 : avion.agent4();
                  break;
               default:
                  throw new IllegalArgumentException("Agent " + numAgent + "inconnu au bataillon");

            }
            sem.release();

         } catch (InterruptedException e) {
            e.printStackTrace();
         }

      }
   }
   public static class tAgentWSem extends Thread{

      AirSimulation avion;
      int numAgent;
      public tAgentWSem(AirSimulation avion, int i) {
         this.avion = avion;
         this.numAgent=i;
      }
      @Override
      public void run() {
         try {
            switch (numAgent){
               case 1 : avion.agent1();
                  break;
               case 2 : avion.agent2();
                  break;
               case 3 : avion.agent3();
                  break;
               case 4 : avion.agent4();
                  break;
               default:
                  throw new IllegalArgumentException("Agent " + numAgent + "inconnu au bataillon");

            }
         } catch (InterruptedException e) {
            e.printStackTrace();
         }

      }
   }
   // Printing
   public String toString()
   {
      String print = "AirSimulation (agent1 : " + this.nAgent1 + ", agent2 : " + this.nAgent2 + ", " +
                                    "agent3 : " + this.nAgent3 + ", agent4 : " + this.nAgent4 + ")\n";
      print = print + a.toString();
      return print;
   }

   // Simulation in sequential (main)
   public static void main(String[] args) throws InterruptedException
   {
      long start = System.currentTimeMillis();
      System.out.println("\n** Sequential execution **\n");
      if (args != null && args.length > 0 && args[0] != null && args[0].equals("animation"))
      {
         AirSimulation s = new AirSimulation();

         Semaphore sem = new Semaphore(1,true);
         Thread t2 = new tAgent(sem,s,2);
         Thread t3 = new tAgent(sem,s,3);
         Thread t4 = new tAgent(sem,s,4);
         /*
         Thread t2 = new tAgentWSem(s,2);
         Thread t3 = new tAgentWSem(s,3);
         Thread t4 = new tAgentWSem(s,4);*/

         while (!s.a.isFlightFull())
         {
            /*
            s.agent1();
            s.agent2();
            s.agent3();
            s.agent4();*/

            sem.acquire();
            s.agent1();
            sem.release();


            t2.start();
            t3.start();
            t4.start();

            System.out.println(s + s.a.cleanString());
            Thread.sleep(100);
         }

         System.out.println(s);
      }
      ///////////////////////////////////////
      else
      {
         AirSimulation s = new AirSimulation();

         Semaphore sem = new Semaphore(1,true);
         Thread t2 = new tAgent(sem,s,2);
         Thread t3 = new tAgent(sem,s,3);
         Thread t4 = new tAgent(sem,s,4);
         /*
         Thread t2 = new tAgentWSem(s,2);
         Thread t3 = new tAgentWSem(s,3);
         Thread t4 = new tAgentWSem(s,4);*/

         while (!s.a.isFlightFull())
         {
            /*
            s.agent1();
            s.agent2();
            s.agent3();
            s.agent4();*/


            sem.acquire();
            s.agent1();
            sem.release();


            t2.run();
            t3.run();
            t4.run();


         }

         System.out.println(s);
      }
      long end = System.currentTimeMillis();

      System.out.println("Start : " + start);
      System.out.println("End : " + end);
      System.out.println("Result = " + (end - start));
   }
}

