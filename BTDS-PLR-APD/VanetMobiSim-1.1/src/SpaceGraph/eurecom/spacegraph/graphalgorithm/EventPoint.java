package eurecom.spacegraph.graphalgorithm;

import java.io.PrintStream;

class EventPoint extends MyPoint {

	EventPoint Prev, Next;
	
	EventPoint(MyPoint mypoint) {
		super(mypoint);
	}

	EventPoint(double x, double y) {
		super(x, y);
	}

	public void insert(EventPoint eventpoint) {
		if(eventpoint.x > x || eventpoint.x == x && eventpoint.y > y) {
			if(Next != null) {
				Next.insert(eventpoint);
				return;
			} 
			else {
				Next = eventpoint;
				eventpoint.Prev = this;
				return;
			}
		}
		if(eventpoint.x != x || eventpoint.y != y || (eventpoint instanceof CirclePoint)) {
			eventpoint.Prev = Prev;
			eventpoint.Next = this;
			
			if(Prev != null) {
				Prev.Next = eventpoint;
			}
			
			Prev = eventpoint;
			
			return;
		} 
		else {
			eventpoint.Prev = eventpoint;
			System.out.println("Double point ignored: " + eventpoint.toString());
			return;
		}
	}

	public void action(Fortune domain) {
		domain.Arcs.insert(this, domain.XPos, domain.Events);
	}
}
