package mgr.jena.recommendation.stereotypebased;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import mgr.jena.gui.SOMColors;
import mgr.jena.osm.OSMUser;

import org.encog.mathutil.matrices.Matrix;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.som.SOM;
import org.encog.neural.som.training.basic.BasicTrainSOM;
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodBubble;

public class SOMNetwork {
	
	private SOM network;
	private BasicTrainSOM train;
	NeighborhoodBubble neighborhoodFunction;
	Point networkSize = new Point(5,4);
	private int initialRadius = 1;
	private int endRadius = 1;
	private double initialLearningRate = 0.0002;
	private double endLearningRate = 0.0001;
	//private int samplesCount = 100;
	//private BufferedImage img;
	private int nodeSize = StereotypeEnum.values().length;
	
	private List<Stereotype> stereotypes;
	
	public String getStereotype(int i)
	{
		return stereotypes.get(i).getName();
		
	}
	
	
	private void initStereotypes()
	{
		stereotypes = new ArrayList<Stereotype>();
		
		//St1 -kinomaniak
		
		Stereotype cinemaman = new Stereotype("cinemaman");
		cinemaman.setValue(StereotypeEnum.cinemas, 1.0);
		stereotypes.add(cinemaman);
		
		//St2 - fast_food
		Stereotype fast_food = new Stereotype("fast_food");
		fast_food.setValue(StereotypeEnum.restaurants_fast_food, 1.0);
		stereotypes.add(fast_food);
		
		//St3 - art
		Stereotype art = new Stereotype("art");
		art.setValue(StereotypeEnum.art, 1.0);
		stereotypes.add(art);
		
		//St4 - party
		Stereotype party = new Stereotype("party");
		party.setValue(StereotypeEnum.party, 1.0);
		stereotypes.add(party);
		
		//St5 - jedzenie na zewnatrz
		Stereotype outdoor_eating = new Stereotype("outdoor_eating");
		outdoor_eating.setValue(StereotypeEnum.outdoor_eating, 1.0);
		stereotypes.add(outdoor_eating);
		
		//St6 - retaurants_american
		Stereotype retaurants_american = new Stereotype("retaurants_american");
		retaurants_american.setValue(StereotypeEnum.restaurants_american, 1.0);
		stereotypes.add(retaurants_american);
		
		//St7 - restaurants_asian
		Stereotype restaurants_asian = new Stereotype("restaurants_asian");
		restaurants_asian.setValue(StereotypeEnum.restaurants_asian, 1.0);
		stereotypes.add(restaurants_asian);
		
		//St8 - reastaurants_italian
		Stereotype reastaurants_italian = new Stereotype("reastaurants_italian");
		reastaurants_italian.setValue(StereotypeEnum.restaurants_italian, 1.0);
		stereotypes.add(reastaurants_italian);
		
		//St9 - restaurants_european
		Stereotype restaurants_european = new Stereotype("restaurants_european");
		restaurants_european.setValue(StereotypeEnum.restaurants_european, 1.0);
		stereotypes.add(restaurants_european);
		
		//St10 - restaurants_vegetarian
		Stereotype restaurants_vegetarian = new Stereotype("restaurants_vegetarian");
		restaurants_vegetarian.setValue(StereotypeEnum.restaurants_vegetarian, 1.0);
		stereotypes.add(restaurants_vegetarian);
		
		//St11 - restaurants_mediterranean
		Stereotype restaurants_mediterranean = new Stereotype("restaurants_mediterranean");
		restaurants_mediterranean.setValue(StereotypeEnum.restaurants_mediterranean, 1.0);
		stereotypes.add(restaurants_mediterranean);
		
		//St12 - restaurants_international
		Stereotype restaurants_international = new Stereotype("restaurants_international");
		restaurants_international.setValue(StereotypeEnum.restaurants_international, 1.0);
		stereotypes.add(restaurants_international);
		
		//St13 - home_eating
		Stereotype home_eating = new Stereotype("home_eating");
		home_eating.setValue(StereotypeEnum.home_eating, 1.0);
		stereotypes.add(home_eating);
		
		//St14 - hotels_expensive
		Stereotype hotels_expensive = new Stereotype("hotels_expensive");
		hotels_expensive.setValue(StereotypeEnum.hotels_expensive, 1.0);
		stereotypes.add(hotels_expensive);
		
		//St15 - hotels_cheap
		Stereotype hotels_cheap = new Stereotype("hotels_cheap");
		hotels_cheap.setValue(StereotypeEnum.hotels_cheap, 1.0);
		stereotypes.add(hotels_cheap);
		
		//St16 - sightseeing
		Stereotype sightseeing = new Stereotype("sightseeing");
		sightseeing.setValue(StereotypeEnum.sightseeing, 1.0);
		stereotypes.add(sightseeing);
		
		//St17 - smoker
		Stereotype smoker = new Stereotype("smoker");
		smoker.setValue(StereotypeEnum.smoker, 1.0);
		stereotypes.add(smoker);
		
		//St18 - internet
		Stereotype internet = new Stereotype("internet");
		internet.setValue(StereotypeEnum.internet, 1.0);
		stereotypes.add(internet);
		
		//St19 - travelman
		Stereotype travelman = new Stereotype("travelman");
		travelman.setValue(StereotypeEnum.travelman, 1.0);
		stereotypes.add(travelman);
		
		//St20 - homeman
		Stereotype homeman = new Stereotype("homeman");
		homeman.setValue(StereotypeEnum.homeman, 1.0);
		stereotypes.add(homeman);
		
	}
	
	/*
	private void initStereotypes()
	{
		stereotypes = new ArrayList<Stereotype>();
		//St1 -student
		Stereotype student = new Stereotype("student");
		student.setValue(StereotypeEnum.amenity_cinema, 1.0);
		student.setValue(StereotypeEnum.amenity_fast_food, 1.0);
		student.setValue(StereotypeEnum.amenity_nightclub, 1.0);
		student.setValue(StereotypeEnum.internet_access, 1.0);
		student.setValue(StereotypeEnum.amenity_pub, 1.0);
		student.setValue(StereotypeEnum.amenity_restaurant, -0.5);
		student.setValue(StereotypeEnum.amenity_cafe, 0.5);
		student.setValue(StereotypeEnum.cuisine_burger, 1.0);
		student.setValue(StereotypeEnum.amenity_theatre, -0.5);
		stereotypes.add(student);
		
		//St2 - biznesmen
		Stereotype businessman = new Stereotype("businessman");
		businessman.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		businessman.setValue(StereotypeEnum.travelman, 1.0);
		businessman.setValue(StereotypeEnum.tourism_hotel, 1.0);
		businessman.setValue(StereotypeEnum.cuisine_international, 1.0);
		stereotypes.add(businessman);
		
		//St3 - Podroznik
		Stereotype travelman = new Stereotype("travelman");
		travelman.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		travelman.setValue(StereotypeEnum.travelman, 1.0);
		travelman.setValue(StereotypeEnum.tourism_hotel, 1.0);
		travelman.setValue(StereotypeEnum.tourism_motel, 1.0);
		travelman.setValue(StereotypeEnum.tourism_attraction, 1.0);
		travelman.setValue(StereotypeEnum.tourism_gallery, 1.0);
		travelman.setValue(StereotypeEnum.tourism_museum, 1.0);
		stereotypes.add(travelman);
		
		//St4 - Domator
		Stereotype homeman = new Stereotype("homeman");
		homeman.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		homeman.setValue(StereotypeEnum.travelman, -1.0);
		homeman.setValue(StereotypeEnum.takeaway, 1.0);
		homeman.setValue(StereotypeEnum.tourism_hostel, -1.0);
		homeman.setValue(StereotypeEnum.tourism_attraction, -1.0);
		homeman.setValue(StereotypeEnum.tourism_gallery, -1.0);
		homeman.setValue(StereotypeEnum.tourism_museum, -1.0);
		homeman.setValue(StereotypeEnum.drive_through, 1.0);
		homeman.setValue(StereotypeEnum.cuisine_regional, 1.0);
		homeman.setValue(StereotypeEnum.cuisine_sandwich, 1.0);
		stereotypes.add(homeman);
		
		//St5 - Milosnik sztuki
		Stereotype artman = new Stereotype("artman");
		artman.setValue(StereotypeEnum.amenity_arts_centre, 1.0);
		artman.setValue(StereotypeEnum.amenity_cinema, 1.0);
		artman.setValue(StereotypeEnum.amenity_theatre, 1.0);
		artman.setValue(StereotypeEnum.tourism_museum, 1.0);
		stereotypes.add(artman);
		
		//St6 - Wegetarianin
		Stereotype vegetarian = new Stereotype("vegetarian");
		vegetarian.setValue(StereotypeEnum.diet_vegetarian, 1.0);
		vegetarian.setValue(StereotypeEnum.cuisine_vegetarian, 1.0);
		vegetarian.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		stereotypes.add(vegetarian);
		
		//St7 - Imprezowicz
		Stereotype partyman = new Stereotype("partyman");
		partyman.setValue(StereotypeEnum.amenity_nightclub, 1.0);
		partyman.setValue(StereotypeEnum.amenity_pub, 1.0);
		partyman.setValue(StereotypeEnum.cuisine_coffee_shop, 1.0);
		partyman.setValue(StereotypeEnum.cuisine_fish_and_chips, 1.0);
		partyman.setValue(StereotypeEnum.cuisine_kebab, 1.0);
		partyman.setValue(StereotypeEnum.amenity_bar, 1.0);
		partyman.setValue(StereotypeEnum.cuisine_breakfast, 1.0);
		stereotypes.add(partyman);
		
		//St8 - Rodzinny
		Stereotype familyman = new Stereotype("familyman");
		familyman.setValue(StereotypeEnum.amenity_ice_cream, 1.0);
		familyman.setValue(StereotypeEnum.amenity_cafe, 1.0);
		familyman.setValue(StereotypeEnum.amenity_cinema, 1.0);
		familyman.setValue(StereotypeEnum.travelman, -1.0);
		familyman.setValue(StereotypeEnum.amenity_nightclub, -1.0);
		familyman.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		familyman.setValue(StereotypeEnum.attraction, 1.0);
		familyman.setValue(StereotypeEnum.smoking, -1.0);
		familyman.setValue(StereotypeEnum.cuisine_breakfast, 1.0);
		familyman.setValue(StereotypeEnum.cuisine_ice_cream, 1.0);
		stereotypes.add(familyman);
		
		//St9 - swieze powietrze
		Stereotype natureman = new Stereotype("natureman");
		natureman.setValue(StereotypeEnum.amenity_ice_cream, 1.0);
		natureman.setValue(StereotypeEnum.outdoor_seating, 1.0);
		natureman.setValue(StereotypeEnum.cuisine_ice_cream, 1.0);
		
		natureman.setValue(StereotypeEnum.amenity_bbq, 1.0);
		natureman.setValue(StereotypeEnum.amenity_cafe, 1.0);
		stereotypes.add(natureman);
		
		//St10 - milosnik kuchni azjatyckiej
		Stereotype asian = new Stereotype("asian");
		asian.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		asian.setValue(StereotypeEnum.amenity_bar, 1.0);
		asian.setValue(StereotypeEnum.cuisine_asian, 1.0);
		asian.setValue(StereotypeEnum.cuisine_chicken, 1.0);
		asian.setValue(StereotypeEnum.cuisine_chinese, 1.0);
		asian.setValue(StereotypeEnum.cuisine_japanese, 1.0);
		asian.setValue(StereotypeEnum.cuisine_thai, 1.0);
		asian.setValue(StereotypeEnum.cuisine_vietnamese, 1.0);
		asian.setValue(StereotypeEnum.cuisine_sushi, 1.0);
		asian.setValue(StereotypeEnum.cuisine_indian, 1.0);
		stereotypes.add(asian);
		
		//St11 - milosnik kuchni europejskiej
		Stereotype european = new Stereotype("european");
		european.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		european.setValue(StereotypeEnum.amenity_bar, 1.0);
		european.setValue(StereotypeEnum.amenity_pub, 1.0);
		european.setValue(StereotypeEnum.cuisine_chicken, 1.0);
		european.setValue(StereotypeEnum.cuisine_coffee, 1.0);
		european.setValue(StereotypeEnum.cuisine_french, 1.0);
		european.setValue(StereotypeEnum.cuisine_fish_and_chips, 1.0);
		european.setValue(StereotypeEnum.cuisine_spanish, 1.0);
		european.setValue(StereotypeEnum.cuisine_portuguese, 1.0);
		stereotypes.add(european);
		
		//St12 - milosnik kuchni srodziemnomorskiej
		Stereotype mediterranean = new Stereotype("mediterranean");
		mediterranean.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		mediterranean.setValue(StereotypeEnum.amenity_bar, 1.0);
		mediterranean.setValue(StereotypeEnum.cuisine_greek, 1.0);
		mediterranean.setValue(StereotypeEnum.cuisine_lebanese, 1.0);
		mediterranean.setValue(StereotypeEnum.cuisine_pizza, 1.0);
		mediterranean.setValue(StereotypeEnum.cuisine_seafood, 1.0);
		stereotypes.add(mediterranean);
		
		//St13 - milosnik kuchni amerykanskiej
		Stereotype american = new Stereotype("american");
		american.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		american.setValue(StereotypeEnum.amenity_bar, 1.0);
		american.setValue(StereotypeEnum.amenity_fast_food, 1.0);
		american.setValue(StereotypeEnum.cuisine_burger, 1.0);
		american.setValue(StereotypeEnum.cuisine_american, 1.0);
		american.setValue(StereotypeEnum.cuisine_steak_house, 1.0);
		american.setValue(StereotypeEnum.cuisine_pizza, 1.0);
		american.setValue(StereotypeEnum.cuisine_mexican, 1.0);
		american.setValue(StereotypeEnum.cuisine_regional, 1.0);
		stereotypes.add(american);
		
		//St14 - milosnik kuchni wloskiej
		Stereotype italian = new Stereotype("italian");
		italian.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		italian.setValue(StereotypeEnum.amenity_bar, 1.0);
		italian.setValue(StereotypeEnum.cuisine_coffee, 1.0);
		italian.setValue(StereotypeEnum.cuisine_coffee_shop, 1.0);
		italian.setValue(StereotypeEnum.cuisine_italian, 1.0);
		italian.setValue(StereotypeEnum.cuisine_pizza, 1.0);
		italian.setValue(StereotypeEnum.cuisine_ice_cream, 1.0);
		italian.setValue(StereotypeEnum.amenity_cafe,1.0);
		italian.setValue(StereotypeEnum.amenity_ice_cream,1.0);
		stereotypes.add(italian);
		
		//St15 - osoba jezdzaca na wozku
		Stereotype wheelchair = new Stereotype("wheelchair");
		wheelchair.setValue(StereotypeEnum.wheelchair, 1.0);
		wheelchair.setValue(StereotypeEnum.takeaway, 1.0);
		wheelchair.setValue(StereotypeEnum.delivery, 1.0);
		stereotypes.add(wheelchair);
		
		//St16 - palacz
		Stereotype smoker = new Stereotype("smoker");
		smoker.setValue(StereotypeEnum.smoking, 1.0);
		stereotypes.add(smoker);
		
		//St17 - poszukiwacz internetu
		Stereotype internetManiac = new Stereotype("internetManiac");
		internetManiac.setValue(StereotypeEnum.internet_access, 1.0);
		internetManiac.setValue(StereotypeEnum.wifi, 1.0);
		internetManiac.setValue(StereotypeEnum.amenity_cafe, 1.0);
		stereotypes.add(internetManiac);
		
		//St18 - budzetowy podróżnik
		Stereotype budgetTravelman = new Stereotype("budgetTravelman");
		budgetTravelman.setValue(StereotypeEnum.travelman, 1.0);
		budgetTravelman.setValue(StereotypeEnum.amenity_fast_food, 1.0);
		budgetTravelman.setValue(StereotypeEnum.amenity_cafe, 1.0);
		budgetTravelman.setValue(StereotypeEnum.tourism_guest_house, 1.0);
		budgetTravelman.setValue(StereotypeEnum.tourism_hostel, 1.0);
		budgetTravelman.setValue(StereotypeEnum.tourism_motel, 1.0);
		stereotypes.add(budgetTravelman);
		
		//St19 - milosnik fast foodow
		Stereotype fastFood = new Stereotype("fastFood");
		fastFood.setValue(StereotypeEnum.cuisine_kebab, 1.0);
		fastFood.setValue(StereotypeEnum.amenity_fast_food, 1.0);
		fastFood.setValue(StereotypeEnum.cuisine_pizza, 1.0);
		fastFood.setValue(StereotypeEnum.amenity_bar, 1.0);
		fastFood.setValue(StereotypeEnum.cuisine_fish_and_chips, 1.0);
		stereotypes.add(fastFood);
		
		//St20 - milosnik dan regionalnych
		Stereotype regional = new Stereotype("regional");
		regional.setValue(StereotypeEnum.cuisine_regional, 1.0);
		stereotypes.add(regional);
		
	}*/
	
	public SOMNetwork() {
		initStereotypes();
		this.network = createNetwork();
		this.train = new BasicTrainSOM(this.network, initialLearningRate, null, neighborhoodFunction);
		train.setForceWinner(false);
	}

	public SOM getNetwork() {
		return this.network;
	}
	
	private SOM createNetwork() {
		neighborhoodFunction = new NeighborhoodBubble(initialRadius);
		SOM result = new SOM(nodeSize,networkSize.x * networkSize.y);
		result.reset();
		Matrix weights = new Matrix(networkSize.x * networkSize.y,nodeSize);
		for(int x = 0; x < stereotypes.size();x++)
		{
			Stereotype s = stereotypes.get(x);
			for(int y = 0; y < s.getValues().size();y++)
			{
				weights.set(x,y,s.getValues().get(y));
			}
		}
		result.setWeights(weights);
		return result;
	}
	
	public void train(Map<String, OSMUser> users)
	{
		List<MLData> samples = new ArrayList<MLData>();
		Iterator<Entry<String, OSMUser>> itUser = users.entrySet().iterator();
	    while (itUser.hasNext()) {
	        Map.Entry<String, OSMUser> pairUser = itUser.next();
	        MLData data = new BasicMLData(nodeSize);
	        Stereotype s = pairUser.getValue().calculateStereotype();
	        for(int i=0;i<nodeSize;i++)
	        {
				data.getData()[i] = s.getValues().get(i);
	        }
	        samples.add(data);
	    }
		this.train.setAutoDecay(users.size(), initialLearningRate, endLearningRate, initialRadius, endRadius);

		for (int i = 0; i < users.size(); i++) {
			//int idx = (int) (Math.random() * samples.size());
			MLData c = samples.get(i);
			this.train.trainPattern(c);
			this.train.autoDecay();
		}
		System.out.println();
		System.out.println("Train method finished");
	}
	
	public int winner(OSMUser user)
	{
		//Matrix m = network.getWeights();
		BasicMLData data = new BasicMLData(nodeSize);
		Stereotype s = user.calculateStereotype();
		
		for(int i=0;i<nodeSize;i++)
        {
			data.getData()[i] = s.getValues().get(i);
			if(i % 5 ==0)
			{
				System.out.print("{"+ i + "} ");
			}
			System.out.print(data.getData()[i] + " , ");
        }
		System.out.println();
		int win = network.classify(data);
		printWeights(win);
		System.out.println(calculateDifference(win,data));
		printWeights(9);
		System.out.println(calculateDifference(9,data));
		return win;
	}
	
	private double calculateDifference(int number, BasicMLData data)
	{
		double difference = 0.0;
		for(int i=0;i<data.size();i++)
		{
			double diff = data.getData()[i] - network.getWeights().get(number,i);
			difference += diff * diff;
		}
		
		return Math.sqrt(difference);
	}
	
	private void printWeights(int number)
	{
		Matrix temp = network.getWeights().getRow(number);
		double[] d = temp.toPackedArray();
		for(int i=0;i<d.length;i++)
		{
			if(i % 5 ==0)
			{
				System.out.print("{"+ i + "} ");
			}
			System.out.print(d[i] + " , ");
		}
		System.out.println();
	}
}
