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
	private int initialRadius = 3;
	private int iterations = 1000; 
	private int endRadius = 1;
	private double initialLearningRate = 0.8;
	private double endLearningRate = 0.001;
	private int samplesCount = 100;
	private BufferedImage img;
	private int nodeSize = StereotypeEnum.values().length;
	
	private List<Stereotype> stereotypes;
	
	private void initStereotypes()
	{
		stereotypes = new ArrayList<Stereotype>();
		//St1 -student
		Stereotype student = new Stereotype();
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
		Stereotype businessman = new Stereotype();
		businessman.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		businessman.setValue(StereotypeEnum.travelman, 1.0);
		businessman.setValue(StereotypeEnum.tourism_hotel, 1.0);
		businessman.setValue(StereotypeEnum.cuisine_international, 1.0);
		stereotypes.add(businessman);
		
		//St3 - Podroznik
		Stereotype travelman = new Stereotype();
		travelman.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		travelman.setValue(StereotypeEnum.travelman, 1.0);
		travelman.setValue(StereotypeEnum.tourism_hotel, 1.0);
		travelman.setValue(StereotypeEnum.tourism_motel, 1.0);
		travelman.setValue(StereotypeEnum.tourism_attraction, 1.0);
		travelman.setValue(StereotypeEnum.tourism_gallery, 1.0);
		travelman.setValue(StereotypeEnum.tourism_museum, 1.0);
		stereotypes.add(travelman);
		
		//St4 - Domator
		Stereotype homeman = new Stereotype();
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
		Stereotype artman = new Stereotype();
		artman.setValue(StereotypeEnum.amenity_arts_centre, 1.0);
		artman.setValue(StereotypeEnum.amenity_cinema, 1.0);
		artman.setValue(StereotypeEnum.amenity_theatre, 1.0);
		artman.setValue(StereotypeEnum.tourism_museum, 1.0);
		stereotypes.add(artman);
		
		//St6 - Wegetarianin
		Stereotype vegetarian = new Stereotype();
		vegetarian.setValue(StereotypeEnum.diet_vegetarian, 1.0);
		vegetarian.setValue(StereotypeEnum.cuisine_vegetarian, 1.0);
		vegetarian.setValue(StereotypeEnum.amenity_theatre, 1.0);
		vegetarian.setValue(StereotypeEnum.tourism_museum, 1.0);
		stereotypes.add(vegetarian);
		
		//St7 - Imprezowicz
		Stereotype partyman = new Stereotype();
		partyman.setValue(StereotypeEnum.amenity_nightclub, 1.0);
		partyman.setValue(StereotypeEnum.amenity_pub, 1.0);
		partyman.setValue(StereotypeEnum.cuisine_coffee_shop, 1.0);
		partyman.setValue(StereotypeEnum.cuisine_fish_and_chips, 1.0);
		partyman.setValue(StereotypeEnum.cuisine_kebab, 1.0);
		partyman.setValue(StereotypeEnum.amenity_bar, 1.0);
		partyman.setValue(StereotypeEnum.cuisine_breakfast, 1.0);
		stereotypes.add(partyman);
		
		//St8 - Rodzinny
		Stereotype familyman = new Stereotype();
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
		Stereotype natureman = new Stereotype();
		natureman.setValue(StereotypeEnum.amenity_ice_cream, 1.0);
		natureman.setValue(StereotypeEnum.outdoor_seating, 1.0);
		natureman.setValue(StereotypeEnum.cuisine_ice_cream, 1.0);
		
		natureman.setValue(StereotypeEnum.amenity_bbq, 1.0);
		natureman.setValue(StereotypeEnum.amenity_cafe, 1.0);
		stereotypes.add(natureman);
		
		//St10 - milosnik kuchni azjatyckiej
		Stereotype asian = new Stereotype();
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
		Stereotype european = new Stereotype();
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
		Stereotype mediterranean = new Stereotype();
		mediterranean.setValue(StereotypeEnum.amenity_restaurant, 1.0);
		mediterranean.setValue(StereotypeEnum.amenity_bar, 1.0);
		mediterranean.setValue(StereotypeEnum.cuisine_greek, 1.0);
		mediterranean.setValue(StereotypeEnum.cuisine_lebanese, 1.0);
		mediterranean.setValue(StereotypeEnum.cuisine_pizza, 1.0);
		mediterranean.setValue(StereotypeEnum.cuisine_seafood, 1.0);
		stereotypes.add(mediterranean);
		
		//St13 - milosnik kuchni amerykanskiej
		Stereotype american = new Stereotype();
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
		Stereotype italian = new Stereotype();
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
		Stereotype wheelchair = new Stereotype();
		wheelchair.setValue(StereotypeEnum.travelman, -1.0);
		wheelchair.setValue(StereotypeEnum.smoking, -1.0);
		wheelchair.setValue(StereotypeEnum.wheelchair, 1.0);
		wheelchair.setValue(StereotypeEnum.takeaway, 1.0);
		wheelchair.setValue(StereotypeEnum.delivery, 1.0);
		stereotypes.add(wheelchair);
		
		//St16 - palacz
		Stereotype smoker = new Stereotype();
		smoker.setValue(StereotypeEnum.smoking, 1.0);
		stereotypes.add(smoker);
		
		//St17 - poszukiwacz internetu
		Stereotype internetManiac = new Stereotype();
		internetManiac.setValue(StereotypeEnum.internet_access, 1.0);
		internetManiac.setValue(StereotypeEnum.wifi, 1.0);
		internetManiac.setValue(StereotypeEnum.amenity_cafe, 1.0);
		stereotypes.add(internetManiac);
		
		//St18 - budzetowy podróżnik
		Stereotype budgetTravelman = new Stereotype();
		budgetTravelman.setValue(StereotypeEnum.travelman, 1.0);
		budgetTravelman.setValue(StereotypeEnum.amenity_fast_food, 1.0);
		budgetTravelman.setValue(StereotypeEnum.amenity_cafe, 1.0);
		budgetTravelman.setValue(StereotypeEnum.tourism_guest_house, 1.0);
		budgetTravelman.setValue(StereotypeEnum.tourism_hostel, 1.0);
		budgetTravelman.setValue(StereotypeEnum.tourism_motel, 1.0);
		stereotypes.add(budgetTravelman);
		
		//St19 - milosnik fast foodow
		Stereotype fastFood = new Stereotype();
		fastFood.setValue(StereotypeEnum.cuisine_kebab, 1.0);
		fastFood.setValue(StereotypeEnum.amenity_fast_food, 1.0);
		fastFood.setValue(StereotypeEnum.cuisine_pizza, 1.0);
		fastFood.setValue(StereotypeEnum.amenity_bar, 1.0);
		fastFood.setValue(StereotypeEnum.cuisine_fish_and_chips, 1.0);
		stereotypes.add(fastFood);
		
		//St20 - milosnik dan regionalnych
		Stereotype regional = new Stereotype();
		regional.setValue(StereotypeEnum.cuisine_regional, 1.0);
		stereotypes.add(regional);
		
	}
	
	public SOMNetwork() {
		initStereotypes();
		this.network = createNetwork();
		this.train = new BasicTrainSOM(this.network, 0.8, null, neighborhoodFunction);
		train.setForceWinner(true);
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
	}
	
	public int winner(OSMUser user)
	{
		//Matrix m = network.getWeights();
		BasicNeuralData data = new BasicNeuralData(nodeSize);
		Stereotype s = user.calculateStereotype();
		
		for(int i=0;i<nodeSize;i++)
        {
			data.getData()[i] = s.getValues().get(i);
        }
		return network.winner(data);
	}
	
	public void run() {

		List<MLData> samples = new ArrayList<MLData>();
		for (int i = 0; i < samplesCount; i++) {
			MLData data = new BasicMLData(3);
			/* Set samples data
			int x = RangeRandomizer.randomInt(0,img.getWidth()-1);
			int y = RangeRandomizer.randomInt(0,img.getHeight()-1);
			
			Color c = new Color(img.getRGB(x, y));
			
			data.setData(0, convertColor(c.getRed()));
			data.setData(1, convertColor(c.getGreen()));
			data.setData(2, convertColor(c.getBlue()));*/
			samples.add(data);
		}
		this.train.setAutoDecay(iterations, initialLearningRate, endLearningRate, initialRadius, endRadius);

		for (int i = 0; i < iterations; i++) {
			int idx = (int) (Math.random() * samples.size());
			MLData c = samples.get(idx);
			this.train.trainPattern(c);
			this.train.autoDecay();
			
		}
		Matrix m = network.getWeights();
		for (int x = 0; x < img.getWidth(); x++)
        {
            for (int y = 0; y < img.getHeight(); y++)
            {
                Color c = new Color(img.getRGB(x, y));
                BasicNeuralData data = new BasicNeuralData(3);
                /*set data
                data.getData()[0] = convertColor(c.getRed());
                data.getData()[1] = convertColor(c.getGreen());
                data.getData()[2] = convertColor(c.getBlue());
                */
                int winner = network.winner(data);
                /*
                MLData result = new BasicMLData(3);
                
                Matrix row = network.getWeights().getRow(winner);
                for (int i = 0; i < 3; i++)
                {
                    result.getData()[i] = row.get(0, i);
                }
                int r = convertColor(result.getData(0));
                int g = convertColor(result.getData(1));
                int b = convertColor(result.getData(2));
                Color newColor = new Color(r,g,b);
                img.setRGB(x, y, newColor.getRGB());
                */
            }
        }
			
	}
}
