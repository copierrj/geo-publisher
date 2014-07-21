package nl.idgis.publisher.client;

import java.util.concurrent.TimeUnit;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import nl.idgis.publisher.database.messages.GetVersion;
import nl.idgis.publisher.database.messages.Version;
import nl.idgis.publisher.utils.Boot;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;

public class App extends UntypedActor {
	
	private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	public static Props props() {
		return Props.create(App.class);
	}
	
	@Override
	public void preStart() throws Exception {
		ActorSelection databaseSelection = getContext().actorSelection("akka.tcp://service@127.0.0.1:2552/user/app/database");
		
		Future<Object> versionBySelection = Patterns.ask(databaseSelection, new GetVersion(), 15000);
		versionBySelection.onSuccess(new OnSuccess<Object>() {

			@Override
			public void onSuccess(Object msg) throws Throwable {
				Version version = (Version)msg;
				log.debug("by selection: " + version);
			}
			
		}, getContext().dispatcher());
		
		databaseSelection.resolveOne(Duration.create(15, TimeUnit.SECONDS))
			.onSuccess(new OnSuccess<ActorRef>() {

			@Override
			public void onSuccess(ActorRef databaseRef) throws Throwable {
				Future<Object> versionByRef = Patterns.ask(databaseRef, new GetVersion(), 15000);
				versionByRef.onSuccess(new OnSuccess<Object>() {

					@Override
					public void onSuccess(Object msg) throws Throwable {
						Version version = (Version)msg;
						log.debug("by ref: " + version);
					}
					
				}, getContext().dispatcher());
			}
			
		}, getContext().dispatcher());
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		
	}
	
	public static void main(String[] args) {
		Boot boot = Boot.init("client");
		boot.startApplication(App.props());
	}

}
