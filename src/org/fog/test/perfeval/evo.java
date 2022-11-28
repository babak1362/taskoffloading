package org.fog.test.perfeval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;

public class evo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Log.printLine("Starting game...");

		try {
			Log.disable();
			int num_user = 1;  // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events
            List<FogDevice> FogList=new ArrayList<FogDevice>();
			CloudSim.init(num_user, calendar, trace_flag);
            int n=100;
            FogDevice fog= createFogDevice("fog", 12000000, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25); 
 //        System.out.println("fog id="+fog.getId());
           fog.setParentId(-1);
           fog.setLevel(0);
           fog.setUplinkLatency(3);
           fog.set_n(n);
           FogList.add(fog);
           for(int i=1;i<=n;i++)
           {
  //             int j=i%4*50000;
        	   FogDevice m= createFogDevice("mobile",getValue(470000,500000), 40000, 100, 10000, 1, 0.01, 16*103, 16*83.25);
 //       	   System.out.println("mobile id="+m.getId());
               m.setLevel(1);
               m.setParentId(fog.getId());
               m.setUplinkLatency(10);
               FogList.add(m);
           }
   		TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
		
		CloudSim.startSimulation();
		
		CloudSim.stopSimulation();

		Log.printLine("Game finished!");
	} catch (Exception e) {
		e.printStackTrace();
		Log.printLine("Unwanted errors happen");
	}
	}
	public static  int getValue(int min, int max)
	{
	       Random r = new Random();	       
	       int randomValue =min+r.nextInt(max-min) ;
//	       System.out.println("random="+randomValue);
	       return randomValue;
	}
	private static FogDevice createFogDevice(String nodeName, long mips,
			int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {
		
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				new FogLinearPowerModel(busyPower, idlePower)
			);

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(nodeName, characteristics, 
					new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fogdevice.setLevel(level);
		return fogdevice;
	}
} 