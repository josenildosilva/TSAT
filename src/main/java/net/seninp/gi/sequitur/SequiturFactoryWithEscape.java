package net.seninp.gi.sequitur;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.gi.logic.GIUtils;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

public class SequiturFactoryWithEscape {

	/** Chunking/Sliding switch action key. */
	protected static final String USE_SLIDING_WINDOW_ACTION_KEY = "sliding_window_key";

	private static final NormalAlphabet normalA = new NormalAlphabet();

	private static SAXProcessor sp = new SAXProcessor();

	// logging stuff
	//
	private static Logger consoleLogger;
	private static Level LOGGING_LEVEL = Level.INFO;

	static {
		consoleLogger = (Logger) LoggerFactory.getLogger(SequiturFactory.class);
		consoleLogger.setLevel(LOGGING_LEVEL);
	}

	/**
	 * Disabling the constructor.
	 */
	private SequiturFactoryWithEscape() {
		assert true;
	}

	public static SAXRule runSequitur(String inputString, int[] startingPositions, ArrayList<Integer> saxWordsIndexes,
                                      int windowSize) {
		consoleLogger.trace("digesting the string " + inputString);

		SAXRule.numRules = new AtomicInteger(0);
		SAXRule.theRules.clear();
		SAXSymbol.theDigrams.clear();
		SAXSymbol.theSubstituteTable.clear();

		SAXRule resRule = new SAXRule();

		StringTokenizer st = new StringTokenizer(inputString, " ");

		int currentPosition = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();

			SAXTerminal symbol = new SAXTerminal(token, currentPosition);

			resRule.last().insertAfter(symbol);
			if ((!checkIntersection(currentPosition, startingPositions, windowSize, saxWordsIndexes).booleanValue())
					&& (!checkIntersection(currentPosition - 1, startingPositions, windowSize, saxWordsIndexes)
							.booleanValue())) {
				resRule.last().p.check();
			}
			currentPosition++;
		}
		return resRule;
	}

	public static Boolean checkIntersection(int currentPosition, int[] startingPositions, int windowSize,
			ArrayList<Integer> saxWordsIndexes) {
		if ((currentPosition < 0) || (currentPosition >= saxWordsIndexes.size())) {
			return Boolean.valueOf(false);
		}
		int start = ((Integer) saxWordsIndexes.get(currentPosition)).intValue();
		int end = start + windowSize - 1;
		int[] arrayOfInt;
		int j = (arrayOfInt = startingPositions).length;
		for (int i = 0; i < j; i++) {
			int startP = arrayOfInt[i];
			if ((startP >= start) && (startP <= end)) {
				return Boolean.valueOf(true);
			}
			if (startP > end) {
				break;
			}
		}
		return Boolean.valueOf(false);
	}

	/**
	 * Digests a string of terminals separated by a space.
	 * 
	 * @param inputString
	 *            the string to digest.
	 * 
	 * @return The top rule handler (i.e. R0).
	 * @throws Exception
	 *             if error occurs.
	 */
	public static SAXRule runSequitur(String inputString) throws Exception {

		consoleLogger.trace("digesting the string " + inputString);

		// clear global collections
		//
		SAXRule.numRules = new AtomicInteger(0);
		SAXRule.theRules.clear();
		SAXSymbol.theDigrams.clear();
		SAXSymbol.theSubstituteTable.clear();

		// init the top-level rule
		//
		SAXRule resRule = new SAXRule();

		// tokenize the input string
		//
		StringTokenizer st = new StringTokenizer(inputString, " ");

		// while there are tokens
		int currentPosition = 0;
		while (st.hasMoreTokens()) {

			String token = st.nextToken();
			// System.out.println(" processing the token " + token);

			// extract next token
			SAXTerminal symbol = new SAXTerminal(token, currentPosition);

			// append to the end of the current sequitur string
			// ... As each new input symbol is observed, append it to rule S....
			resRule.last().insertAfter(symbol);

			// once appended, check if the resulting digram is new or recurrent
			//
			// ... Each time a link is made between two symbols if the new
			// digram is repeated elsewhere
			// and the repetitions do not overlap, if the other occurrence is a
			// complete rule,
			// replace the new digram with the non-terminal symbol that heads
			// the rule,
			// otherwise,form a new rule and replace both digrams with the new
			// non-terminal symbol
			// otherwise, insert the digram into the index...
			resRule.last().p.check();

			currentPosition++;

			// consoleLogger.debug("Current grammar:\n" + SAXRule.getRules());
		}

		return resRule;
	}

	// /**
	// * Digests a string of symbols separated by space.
	// *
	// * @param string The string to digest. Symbols expected to be separated by
	// space.
	// * @param alphabetSize The alphabet size to use.
	// * @param threshold the merging distance threshold to use.
	// *
	// * @return The top rule handler.
	// *
	// * @throws Exception if error occurs.
	// */
	// public static SAXRule runSequiturWithEditDistanceThreshold(String string,
	// Integer alphabetSize,
	// Integer threshold) throws Exception {
	//
	// consoleLogger.trace("digesting the string " + string);
	//
	// // clear global collections
	// //
	// SAXRule.numRules = new AtomicInteger(0);
	// SAXRule.theRules.clear();
	// SAXSymbol.theDigrams.clear();
	// SAXSymbol.theSubstituteTable.clear();
	//
	// // init the top-level rule
	// //
	// SAXRule resRule = new SAXRule();
	//
	// // tokenize the input string
	// //
	// StringTokenizer st = new StringTokenizer(string, " ");
	//
	// // while there are tokens
	// int currentPosition = 0;
	// while (st.hasMoreTokens()) {
	//
	// String token = st.nextToken();
	// // System.out.println(" processing the token " + token);
	//
	// if (null != alphabetSize && null != threshold) {
	// //
	// boolean merged = false;
	// for (String str : SAXSymbol.theSubstituteTable.keySet()) {
	// double dist = sp.saxMinDist(str.toCharArray(), token.toCharArray(),
	// normalA.getDistanceMatrix(alphabetSize));
	// if (dist < threshold) {
	// merged = true;
	// SAXSymbol.theSubstituteTable.get(str).put(token.substring(0),
	// currentPosition);
	// token = str;
	// }
	// }
	// if (!(merged)) {
	// SAXSymbol.theSubstituteTable.put(token, new Hashtable<String,
	// Integer>());
	// }
	// }
	//
	// // extract next token
	// SAXTerminal symbol = new SAXTerminal(token, currentPosition);
	//
	// // append to the end of the current sequitur string
	// // ... As each new input symbol is observed, append it to rule S....
	// resRule.last().insertAfter(symbol);
	//
	// // once appended, check if the resulting digram is new or recurrent
	// //
	// // ... Each time a link is made between two symbols if the new digram is
	// repeated elsewhere
	// // and the repetitions do not overlap, if the other occurrence is a
	// complete rule,
	// // replace the new digram with the non-terminal symbol that heads the
	// rule,
	// // otherwise,form a new rule and replace both digrams with the new
	// non-terminal symbol
	// // otherwise, insert the digram into the index...
	// resRule.last().p.check();
	// currentPosition++;
	//
	// consoleLogger.trace("Current grammar:\n" + SAXRule.printRules());
	// }
	//
	// return resRule;
	// }

	/**
	 * Takes a time series and returns a grammar.
	 * 
	 * @param timeseries
	 *            the input time series.
	 * @param saxWindowSize
	 *            the sliding window size.
	 * @param saxPAASize
	 *            the PAA num.
	 * @param saxAlphabetSize
	 *            the SAX alphabet size.
	 * @param numerosityReductionStrategy
	 *            the SAX Numerosity Reduction strategy.
	 * @param normalizationThreshold
	 *            the SAX normalization threshod.
	 * @return the set of rules, i.e. the grammar.
	 * @throws Exception
	 *             if error occurs.
	 * @throws IOException
	 *             if error occurs.
	 */
	public static GrammarRules series2SequiturRules(double[] timeseries, int saxWindowSize, int saxPAASize,
                                                    int saxAlphabetSize, NumerosityReductionStrategy numerosityReductionStrategy, double normalizationThreshold)
					throws Exception, IOException {

		consoleLogger.debug("Discretizing time series...");

		SAXRecords saxFrequencyData = sp.ts2saxViaWindow(timeseries, saxWindowSize, saxPAASize,
				normalA.getCuts(saxAlphabetSize), numerosityReductionStrategy, normalizationThreshold);

		consoleLogger.debug("Inferring the grammar...");

		// this is a string we are about to feed into Sequitur
		//
		String saxDisplayString = saxFrequencyData.getSAXString(" ");

		// String[] split = saxDisplayString.split(" ");
		// System.out.println("*** " + split[21] + " " + split[22] + " " +
		// split[23]);

		// BufferedWriter bw = new BufferedWriter(new FileWriter(new
		// File("rules_stat.txt")));

		// reset Sequitur structures
		SAXRule.numRules = new AtomicInteger(0);
		SAXRule.theRules.clear();
		SAXSymbol.theDigrams.clear();

		SAXRule grammar = new SAXRule();
		SAXRule.arrRuleRecords = new ArrayList<GrammarRuleRecord>();

		StringTokenizer st = new StringTokenizer(saxDisplayString, " ");
		int currentPosition = 0;
		while (st.hasMoreTokens()) {
			grammar.last().insertAfter(new SAXTerminal(st.nextToken(), currentPosition));
			grammar.last().p.check();
			// bw.write(currentPosition + "," + SAXSymbol.theDigrams.size() +
			// "," +
			// SAXRule.theRules.size()
			// + "\n");
			currentPosition++;
		}

		// bw.close();
		consoleLogger.debug("Collecting the grammar rules statistics and expanding the rules...");
		GrammarRules rules = grammar.toGrammarRulesData();

		consoleLogger.debug("Mapping expanded rules to time-series intervals...");
		SequiturFactory.updateRuleIntervals(rules, saxFrequencyData, true, timeseries, saxWindowSize, saxPAASize);

		return rules;

	}

	public static void updateRuleIntervals(GrammarRules rules, SAXRecords saxFrequencyData, boolean slidingWindowOn,
                                           double[] originalTimeSeries, int saxWindowSize, int saxPAASize) {

		// the original indexes of all SAX words
		ArrayList<Integer> saxWordsIndexes = new ArrayList<Integer>(saxFrequencyData.getAllIndices());

		for (GrammarRuleRecord ruleContainer : rules) {

			// here we construct the array of rule intervals
			ArrayList<RuleInterval> resultIntervals = new ArrayList<RuleInterval>();

			// array of all words of this rule expanded form
			// String[] expandedRuleSplit =
			// ruleContainer.getExpandedRuleString().trim().split(" ");
			int expandedRuleLength = countSpaces(ruleContainer.getExpandedRuleString());

			// the auxiliary array that keeps lengths of all rule occurrences
			int[] lengths = new int[ruleContainer.getOccurrences().size()];
			int lengthCounter = 0;

			// iterate over all occurrences of this rule
			// the currentIndex here is the position of the rule in the input
			// string
			//
			for (Integer currentIndex : ruleContainer.getOccurrences()) {

				// System.out.println("Index: " + currentIndex);
				// String extractedStr = "";

				// what we do here is to extract the positions of sax words in
				// the real time-series
				// by using their positions at the input string
				//
				// int[] extractedPositions = new int[expandedRuleSplit.length];
				// for (int i = 0; i < expandedRuleSplit.length; i++) {
				// extractedStr = extractedStr.concat(" ").concat(
				// saxWordsToIndexesMap.get(saxWordsIndexes.get(currentIndex +
				// i)));
				// extractedPositions[i] = saxWordsIndexes.get(currentIndex +
				// i);
				// }

				int startPos = saxWordsIndexes.get(currentIndex);
				int endPos = -1;
				if ((currentIndex + expandedRuleLength) >= saxWordsIndexes.size()) {
					endPos = originalTimeSeries.length;
				} else {
					if (slidingWindowOn) {
						endPos = saxWordsIndexes.get(currentIndex + expandedRuleLength) + saxWindowSize - 1;
					} else {
						double step = (double) originalTimeSeries.length / (double) saxPAASize;
						endPos = Long.valueOf(Math.round(startPos + expandedRuleLength * step)).intValue();
					}
				}

				resultIntervals.add(new RuleInterval(startPos, endPos));

				lengths[lengthCounter] = endPos - startPos;
				lengthCounter++;
			}
			if (0 == ruleContainer.getRuleNumber()) {
				resultIntervals.add(new RuleInterval(0, originalTimeSeries.length));
				lengths = new int[1];
				lengths[0] = originalTimeSeries.length;
			}
			ruleContainer.setRuleIntervals(resultIntervals);
			ruleContainer.setMeanLength((int) GIUtils.mean(lengths));
			ruleContainer.setMinMaxLength(lengths);
		}

	}

	/**
	 * Recovers start and stop coordinates of a rule subsequences.
	 * 
	 * @param ruleIdx
	 *            The rule index.
	 * @param grammar
	 *            The grammar to analyze.
	 * @param saxFrequencyData
	 *            the SAX frquency data used for the grammar construction.
	 * @param originalTimeSeries
	 *            the original time series.
	 * @param saxWindowSize
	 *            the SAX sliding window size.
	 * @return The array of all intervals corresponding to this rule.
	 */
	public static ArrayList<RuleInterval> getRulePositionsByRuleNum(int ruleIdx, SAXRule grammar,
                                                                    SAXRecords saxFrequencyData, double[] originalTimeSeries, int saxWindowSize) {

		// this will be the result
		ArrayList<RuleInterval> resultIntervals = new ArrayList<RuleInterval>();

		// the rule container
		GrammarRuleRecord ruleContainer = grammar.getRuleRecords().get(ruleIdx);

		// the original indexes of all SAX words
		ArrayList<Integer> saxWordsIndexes = new ArrayList<Integer>(saxFrequencyData.getAllIndices());

		// debug printout
		consoleLogger.trace("Expanded rule: \"" + ruleContainer.getExpandedRuleString() + '\"');
		consoleLogger.trace("Indexes: " + ruleContainer.getOccurrences());

		// array of all words of this expanded rule
		String[] expandedRuleSplit = ruleContainer.getExpandedRuleString().trim().split(" ");

		for (Integer currentIndex : ruleContainer.getOccurrences()) {

			String extractedStr = "";
			StringBuffer sb = new StringBuffer(expandedRuleSplit.length);
			for (int i = 0; i < expandedRuleSplit.length; i++) {
				consoleLogger.trace("currentIndex " + currentIndex + ", i: " + i);
				extractedStr = extractedStr.concat(" ").concat(String
						.valueOf(saxFrequencyData.getByIndex(saxWordsIndexes.get(currentIndex + i)).getPayload()));
				sb.append(saxWordsIndexes.get(currentIndex + i)).append(" ");
			}
			consoleLogger.trace("Recovered string: " + extractedStr);
			consoleLogger.trace("Recovered positions: " + sb.toString());

			int start = saxWordsIndexes.get(currentIndex);
			int end = -1;
			// need to care about bouncing beyond the all SAX words index array
			if ((currentIndex + expandedRuleSplit.length) >= saxWordsIndexes.size()) {
				// if we at the last index - then it's easy - end is the
				// timeseries end
				end = originalTimeSeries.length - 1;
			} else {
				// if we OK with indexes, the Rule subsequence end is the start
				// of the very next SAX word
				// after the kast in this expanded rule
				end = saxWordsIndexes.get(currentIndex + expandedRuleSplit.length) - 1 + saxWindowSize;
			}
			// save it
			resultIntervals.add(new RuleInterval(start, end));
		}

		return resultIntervals;
	}

	/**
	 * Counts spaces in the string.
	 * 
	 * @param str
	 *            The string.
	 * @return The number of spaces.
	 */
	private static int countSpaces(String str) {
		int counter = 0;
		for (char c : str.toCharArray()) {
			if (c == ' ') {
				counter++;
			}
		}
		return counter;
	}

}
