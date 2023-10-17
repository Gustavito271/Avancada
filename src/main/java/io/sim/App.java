package io.sim;

import java.util.ArrayList;
import io.sim.Rota;

import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Atribuição de Endereços de IP:
 *      -> 192.168.1.X = Endereços para os DRIVERS (Motoristas)
 *      -> 192.168.2.X = Endereços para os CARS (Carros)
 */
public class App {

    //Número de motoristas (e consequentemente o número de carros)
    private static final int num_drivers = 100;

    public static void main( String[] args ) {

        SumoTraciConnection sumo;
        String sumo_bin = "sumo-gui";		
		String config_file = "sim/map/map.sumo.cfg";
		
		// Sumo connection
		sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end

        // try {
        //     sumo.runServer(12345);
        // } catch (Exception e) {
        //     System.out.println("Erro ao iniciar o Sumo.\nException: " + e);
        // }
        
        AlphaBank.main(args);

        Company.main(args);

        //Inicialização de todos os motoristas e seus carros.
        ArrayList<Driver> drivers = new ArrayList<>();
        ArrayList<Car> cars = new ArrayList<>();

        for (int i = 1; i <= num_drivers; i++) {
            String id_car = "Car_" + i;
            String IP_car = Constantes.IP_CAR + i;
            Car car = new Car(id_car, IP_car, sumo);
            cars.add(car);
            
            String id_driver = "Driver_" + i;
            String IP_driver = Constantes.IP_DRIVER + i;
            Driver driver = new Driver(id_driver, IP_driver, car);
            drivers.add(driver);
        }

        drivers.get(0).start();
        // try {
        //     for (int i = 0; i < num_drivers; i++) {
        //         drivers.get(i).start();
        //         //drivers.get(i).join();
        //     }
        // } catch (Exception e) {
        //     System.out.println("Erro ao iniciar as Thread dos Drivers.\nException: " + e);
        // }
        


        //EnvSimulator ev = new EnvSimulator();
        //ev.start();

    }
}