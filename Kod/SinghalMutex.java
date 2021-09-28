import java.util.*;

public class SinghalMutex extends Process implements Lock
{
	//skupovi R i I
	Set<Integer> R = new HashSet<Integer>();
	Set<Integer> I = new HashSet<Integer>();
	
	//varijable Requesting, Executing, My_priority
	boolean Requesting, Executing, My_priority;
	
	//Sat
	int C;
	
	//pomocna varijabla za My_priority
	int priority;
	
	
	public SinghalMutex(Linker initComm)
	{
		super(initComm);
		
		Requesting = false;
		Executing = false;
		My_priority = false;
		
		C=0;
		
		R = new HashSet<Integer>();
		I = new HashSet<Integer>();
		
		for(int i=0; i<myId; i++)
		{
			R.add(i);
		}
	}
	
	public synchronised void requestCS()
	{
		Requesting = true;
		priority=C;
		C=C+1;
		
		for(int i : R)
		{
			sendMsg(i, "request", C);
		}
		
		while(!R.isEmpty)
		{
			myWait();
		}
		
		Requesting = false;
		Executing = true;
	}
	
	public synchronised void releaseCS()
	{
		Executing = false;
		
		Iterator<Integer> it;
		while(!I.isEmpty())
		{
			it=I.iterator();
			i= it.next();
			
			sendMsg(i, "reply", C);
			
			I.remove(i);
			R.add(i);
		}
	}
	
	public synchronised void handleMsg(Msg m, int src, String tag)
	{
		int Ck=m.getMessageInt();
		C=max(C, Ck);
		
		if(tag.equals("request"))
		{
			
			//tri slucaja
			//prvi
			if(Requesting)
			{
				//prvo provjeri prioritet
				My_priority=(priority<Ck || (priority==Ck && myId<src));
				if(My_priority)
				{
					I.add(src);
				}
				else
				{
					sendMsg(src, "reply", C);
					if(!R.contains(src))
					{
						R.add(src);
						sendMsg(src, "request", C);
					}
				}
			}
			//drugi slucaj
			else if(Executing)
			{
				I.add(src);
			}
			//treci slucaj
			else //Requesting == fase i Executing == false
			{
				R.add(src);
				sendMsg(src, "reply", C);
			}
		}
		
		if(tag.equals("reply"))
		{
			R.remove(src);
			//notify?
		}
	}
}