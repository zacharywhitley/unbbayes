package com.Tuuyi.TuuyiOntologyServer;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Tuuyi.TuuyiOntologyServer.generatedClasses.TuuyiOntologyServer.LexicalForm;
import com.Tuuyi.TuuyiOntologyServer.generatedClasses.TuuyiOntologyServer.LexicalForm.Context;
import com.Tuuyi.TuuyiOntologyServer.generatedClasses.TuuyiOntologyServer.LexicalForm.LexicalFormCacheEntry;
import com.Tuuyi.TuuyiOntologyServer.generatedClasses.TuuyiOntologyServer.Relation;
/** 
 * A Simple Java remote client for the Tuuyi Ontology Servlet.
 * The HTML web interface can be accessed at (http://q.tuuyi.net:8080/TuuyiOntologyServlet/ontology.html).
 * The web servlet API can be accessed for example at (http://q.tuuyi.net:8080/TuuyiOntologyServlet/api/term/read/?name=Science&relations=true).
 * Primary external API (see each method for details):

 * OntologyClient

 * getTermById
 * getTermBySimpleName
 * getTermAncestors
 * getTermDescendantCount
 * getTermDepth
 * getLfe
 * getLfes
 * getRelation
 * 
 * createTerm
 * createLexicalForm
 * createRelation

 * inContext
 * mapText
 *
 */
import com.Tuuyi.TuuyiOntologyServer.generatedClasses.TuuyiOntologyServer.Term;
public class OntologyClient {
	
  /** 
   * This is the name of JSON parameter that holds a json object which is a map with known inverse relationships and most relevant concepts related with such relationship.
   * This can be used, for example, in order to obtain less broader (inverse of core#broader) concepts. 
   */
  public static final String INVERSE_RELATION_JSON_PARAMETER = "inverseRelations";
  
  /** 
   * This is the name of JSON parameter that holds a json object which is a map with known relationships and most relevant concepts related with such relationship.
   * This can be used, for example, in order to obtain broader (core#broader) concepts. 
   */
  public static final String RELATION_JSON_PARAMETER = "relations";

  protected Logger logWriter = Logger.getLogger(OntologyClient.class.getClass());
  
  /* set the serverURL to one of the global constants in the client object constructor call */
  private URL serverURL = null; //"http://localhost:8080/TuuyiOntologyServlet/api/";
  public static String LOCAL_SERVLET_URL = "http://localhost:8080/TuuyiOntologyServlet/api/";
  public static String TUUYI_SERVLET_URL = "http://q.tuuyi.net:8080/TuuyiOntologyServlet/api/";
  
  /* the current client if one has been instatiated. 
   * At the moment only a single client is supported for a jvm
   * Multiple clients may be supported at some point in the future
   */
  public static OntologyClient workspaceClient = null;

  /* various local caches to minimize client-server comm */
  protected HashMap <Integer, Term> termIdCache = new HashMap<Integer, Term> ();
  protected HashMap <String, Term> termSimpleNameCache = new HashMap<String, Term> ();
  protected HashMap <Integer, Integer> termDescendantCountCache = new HashMap<Integer, Integer> ();
  protected HashMap <Integer, Integer> termDepthCache = new HashMap<Integer, Integer> ();
  protected HashMap <Integer, ArrayIntList> termAncestorsCache = new HashMap<Integer, ArrayIntList> ();
  
  /** 
   * Cache of mappings from term ID to a mapping from property ID to related terms.
   * For example, if ids of term A, B, C are respectively 1, 2, and 3; and the properties
   * skol:broader and (core:subject) are respectively 10,11, and
   * A->skol:broader->B; A->core:subject->C; 
   * B->skol:broader->C; B->core:subject->C; 
   * then the mapping will be
   * 1->{10->2, 11->3},
   * 2->{10->3, 11->3}.
   */
  private Map<Integer, Map<Integer,List<Integer>>> termToPropertyIdCache = new HashMap<Integer, Map<Integer,List<Integer>>>();
  
  /**This is similar to {@link #termToPropertyIdCache}, but it stores inverse properties.*/
  private Map<Integer, Map<Integer,List<Integer>>> termToInversePropertyIdCache = new HashMap<Integer, Map<Integer,List<Integer>>>();
  
  /* termIds for the key relational terms, these will be initialized at startup */
  public int subjectRelationId = -1;
  public int broaderRelationId = -1;
  public int typeRelationId = -1;
  public int relatedRelationId = -1;
  public Term organizationsTerm = null;
  
  /**
   * @param a_serverURL - one of the public constants, or the URL for another server if you are hosting one
   * @throws MalformedURLException
   */
  public OntologyClient(String  a_serverURL) throws MalformedURLException {
    setServerURL(new URL(a_serverURL));
    initialize();
    workspaceClient = this;
  }

  /**
   * Default constructor, which connects to default URL: {@link #TUUYI_SERVLET_URL}
   */
  public OntologyClient() {
	try {
		setServerURL(new URL(TUUYI_SERVLET_URL));
	} catch (MalformedURLException e) {
		throw new RuntimeException("Could not connect to TUUYI server from " + TUUYI_SERVLET_URL,e);
	}
	initialize();
	workspaceClient = this;
  }

/**
   * This should probably be private, no need to ever call it, called by constructor
   */
  public void initialize() {
    if (subjectRelationId == -1) {
      // initialize relationIds;
      subjectRelationId = getTermBySimpleName("subject").getId();
      broaderRelationId = getTermBySimpleName("core#broader").getId();
      typeRelationId = getTermBySimpleName("22-rdf-syntax-ns#type").getId();
      relatedRelationId = getTermBySimpleName("core#related").getId();
      organizationsTerm = getTermBySimpleName("Category:Organizations");
    }
  }

  /** you can use this for an unsupported request, but probably easier to use one of the higher level API calls below 
   * @param parameters - the html get parameters for this server request
   * @return
   */
  public int remoteIntRequest(String parameters) {
    String response = remoteRequest(parameters);
    if (response != null) {
      try {
        return Integer.parseInt(response.trim());
      } catch (NumberFormatException nfe) {
        logWriter.error("Non-numeric response from OntologyServlet");
      }
    } 
    return -1;
  }

  public JSONObject remoteJSONRequest(String parameters) {
    String response = remoteRequest(parameters);
    if (response != null) {
      try {
        return new JSONObject(response.trim());
      } catch (JSONException nfe) {
        logWriter.error("Non-JSON response from OntologyServlet", nfe);
      }
    } 
    return null;
  }

  public String remoteRequest(String parameters) {
    URL url;
    HttpURLConnection connection = null;  
    try {
      //Create connection
      url = new URL(getServerURL()+parameters);
      BufferedReader rd = new BufferedReader(new InputStreamReader(url.openStream()));
      String line;
      StringBuffer response = new StringBuffer(); 
      while((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      rd.close();
      return response.toString();
    } catch (Exception e) {
      logWriter.error("error in remote request "+parameters, e);
      return null;
    } finally {
      if(connection != null) {
        connection.disconnect(); 
      }
    }
  }
  
 
  /**
   * @param nodeText - the text to be scanned for terms
   * @param context - constraints to focus the scan
   *     public Context(ArrayIntList a_requiredCategories, ArrayIntList a_preferredCategories, ArrayIntList a_disfavoredCategories, ArrayIntList a_excludedCategories, ArrayIntList a_includeTypes, ArrayIntList a_excludeTypes, int a_prune) {
   *       any or all args can be null. 
   *       "Categories" are those related by the 'broader' relation, transitive
   *       "types" are those related by the "type" relation, again transitive
   *     
   * @return - a TextMap - a structured representation of the set of Terms found in a text.
   */
  public TextMap mapText(String nodeText, LexicalForm.Context context) { 
    try {
      String response = remoteRequest("phrase/map/?query="+URLEncoder.encode(nodeText)+context.toURLParams());
      JSONObject responseJSON = new JSONObject(response.trim());
      TextMap map = new TextMap(responseJSON.getJSONObject("textMap"));
      //responseObj:"query", query;
      //responseObj""phraseLookup", LexicalForm.termsAsJSON(terms);
      //responseObj:"textMap", new TextMap(terms, emptyContext).asJSON();
      return map;
    } catch (Exception je) {
      logWriter.error("bad response to text map request", je);
      return null;
    }
  }
  
  /**
   * 
   * @param name - the exact name of the term to be retrieved
   * @return - returns a Term object or null
   */
  public Term getTermBySimpleName(String name) { 
    Term term = termSimpleNameCache.get(name);
    if (term != null) {
      return term;
    }
    
    JSONObject json = remoteJSONRequest("term/read/?name="+URLEncoder.encode(name)+"&relations=true");
    try {
      term = localCloneTerm(json);
      term.setId(json.getInt("id"));
      term.updateFromJSON(json);
      termSimpleNameCache.put(term.getSimpleName(), term);
      termIdCache.put(json.getInt("id"), term);
      return term;
    } catch (JSONException je) {
      logWriter.error("bad response to getTermBySimpleName request", je);
      return null;
    }
  }
  
  /**
   * 
   * @param name - the external string to be searched for
   * @param termId - the term id to be matched (an external string can be an index to many terms)
   * @return - the lexical form - including it's weight
   * 
   * note that mapText above uses an index over Lucene stemmer reductions of the LFE names, so the actual
   *  set of strings that can map to the term identified is not only the literal string provided
   */
  public LexicalFormCacheEntry getLfe(String name, int termId) { 
    String response = remoteRequest("form/read/?form="+URLEncoder.encode(name)+"&termId="+termId);
    try {
      JSONObject json = new JSONObject(response);
      LexicalFormCacheEntry lfe = new LexicalFormCacheEntry(json);
      return lfe;
    } catch (JSONException je) {
      logWriter.error("bad response to getLfe request", je);
      return null;
    }
   }
  
  /**
   * 
   * @param name the external string to be searched for
   * @return - the set of LFEs for this string
   */
  public ArrayList<LexicalFormCacheEntry> getLfes(String name) { 
    try {
      JSONObject json = remoteJSONRequest("form/all/?form="+URLEncoder.encode(name));
      if (json != null) {
        JSONArray jsonLfes = json.getJSONArray("lfes");
        ArrayList<LexicalFormCacheEntry> lfes = new ArrayList<LexicalFormCacheEntry>();
        for (int i = 0; i < json.getJSONArray("lfes").length(); i++) {
          lfes.add(new LexicalFormCacheEntry(jsonLfes.getJSONObject(i)));
        }
        return lfes;
      }
    }catch (JSONException je) {
      logWriter.error("bad response to getLfes request", je);
    }
    return null;
  }
  
  /**
   * 
   * @param form - the external string (surface form) to be added to the ontology server
   * @param termId - the id of the term to be indexed under this string
   * @param pmi - the normalized likelihood of this term given this surface form 
   * @return
   * 
   * note - the definition of pmi above would imply that, when adding an LFE, 
   *   all the other LFEs for the same surface form should change. 
   */
  public LexicalForm.LexicalFormCacheEntry createLexicalForm(String form, int termId, double pmi) {
    String response = remoteRequest("form/create/?form="+form+"&termId="+termId+"&pmi="+pmi);
    try {
      JSONObject json = new JSONObject(response);
      LexicalFormCacheEntry lfe = new LexicalFormCacheEntry(json);
      return lfe;
    } catch (JSONException je) {
      logWriter.error("bad response to getLfe request", je);
      return null;
    }
  }
  
  /**
   * 
   * @param term1Id - the id of the first term (subject) of a relation triple
   * @param relationId - the id of the second (relation) term of a relation triple
   * @param term2Id - the id of the third (object) term of a relation triple
   * @return - the corresponding relation or null
   * 
   * mostly useful to test for relation existence. This is non-transitive
   */
  public Relation getRelation(int term1Id, int relationId, int term2Id) { 
    String response = remoteRequest("relation/read/?term1=termId1&relation=relationId&term2=termId2");
    try {
      JSONObject json = new JSONObject(response);
      Relation relation = new Relation();
      relation.updateFromJSON(json);
      if (relation.getId() == -1) {
        return null; // not found
      }
      return relation;
    } catch (JSONException je) {
      logWriter.error("bad response to getRelation request", je);
      return null;
    }
   }
  
  /**
   * 
   * @param term1Id - the id of the first term (subject) of a relation triple
   * @param relationId - the id of the second (relation) term of a relation triple
   * @param term2Id - the id of the third (object) term of a relation triple
   * @return - the corresponding relation or null if the create fails
   */
  public Relation createRelation(int term1Id, int relationId, int term2Id) { 
    String response = remoteRequest("relation/create/?term1="+term1Id+"&relation="+relationId+"&term2="+term2Id);
    try {
      JSONObject json = new JSONObject(response);
      Relation relation = new Relation();
      relation.updateFromJSON(json);
      return relation;
    } catch (JSONException je) {
      logWriter.error("bad response to createRelation request", je);
      return null;
    }
   }

  
  /**
   * 
   * @param name - the string (surface form) to be used in a search for best matching term (highest pmi)
   * @return - the highest matching term, or null
   */
  public Term matchTermByName(String name) { 
//    JSONObject json = remoteJSONRequest("term/read/?match="+URLEncoder.encode(name));
    JSONObject json = remoteJSONRequest("term/read/?match="+URLEncoder.encode(name));
      Term term = localCloneTerm(json);
      return term;
  }
  
  Term localCloneTerm(JSONObject json) {
    try {
      int idTest= json.optInt("id", -1);
      if (idTest == -1) {
        return null;
      }
      int termId = json.getInt("id");
      Term term = new Term();
      term.setId(termId);
      term.updateFromJSON(json);
      termIdCache.put(termId, term);
      termSimpleNameCache.put(term.getSimpleName(), term);
      termDescendantCountCache.put(termId, json.getInt("descendantCount")); // updateFromJSON is auto-generated and doesn't do this!
      termDepthCache.put(termId, json.getInt("depth")); // updateFromJSON is auto-generated and doesn't do this!
      
      // check relationships (e.g. concepts related by core#broader)
      try {
    	// first, extract all normal (non-inverse) relations
          JSONObject inverseRelations = json.optJSONObject(RELATION_JSON_PARAMETER);
    	  if (inverseRelations != null) {
    		  Iterator iterator = inverseRelations.keys();
    		  while (iterator.hasNext()) {
    			  // extract the relationship from the inverse relations
    			  Object relationshipId = iterator.next();
    			  Integer relationshipIdAsInteger = null;
				  try {
					  relationshipIdAsInteger = Integer.parseInt((String) relationshipId);
				  } catch (NumberFormatException e) {
					  logWriter.error("Was not able to load relationship " + relationshipId + " of term " + termId, e);
				  }
    			  JSONArray jsonValues = inverseRelations.getJSONArray(relationshipIdAsInteger.toString());
    			  if (jsonValues != null) {
    				  // prepare array to be used in order to fill cache
    				  List<Integer> valueList = new ArrayList(jsonValues.length());
    				  for (int i = 0; i < jsonValues.length(); i++) {
    					  valueList.add(jsonValues.getInt(i));
    				  }
    				  // fill the cache, because all methods in this client access concepts through cached objects
    				  Map<Integer, List<Integer>> properties = getTermToPropertyIdCache().get(termId);
    				  if (properties == null) {
    					  properties = new HashMap<Integer, List<Integer>>();
    				  }
    				  properties.put(relationshipIdAsInteger, valueList);
    				  getTermToPropertyIdCache().put(termId, properties);
    			  }
    		  }
    	  }
	  } catch (Exception e) {
		  logWriter.error("Was not able to load relationships of " + termId, e);
	  }
      try {
    	  // then, extract all inverse relations
    	  JSONObject inverseRelations = json.optJSONObject(INVERSE_RELATION_JSON_PARAMETER);
    	  if (inverseRelations != null) {
    		  Iterator iterator = inverseRelations.keys();
    		  while (iterator.hasNext()) {
    			  // extract the relationship from the inverse relations
    			  Object relationshipId = iterator.next();
    			  Integer relationshipIdAsInteger = null;
    			  try {
    				  relationshipIdAsInteger = Integer.parseInt((String) relationshipId);
    			  } catch (NumberFormatException e) {
    				  logWriter.error("Was not able to load relationship " + relationshipId + " of term " + termId, e);
    			  }
    			  JSONArray jsonValues = inverseRelations.getJSONArray(relationshipIdAsInteger.toString());
    			  if (jsonValues != null) {
    				  // prepare array to be used in order to fill cache
    				  List<Integer> valueList = new ArrayList(jsonValues.length());
    				  for (int i = 0; i < jsonValues.length(); i++) {
    					  valueList.add(jsonValues.getInt(i));
    				  }
    				  // fill the cache, because all methods in this client access concepts through cached objects
    				  Map<Integer, List<Integer>> properties = getTermToInversePropertyIdCache().get(termId);
    				  if (properties == null) {
    					  properties = new HashMap<Integer, List<Integer>>();
    				  }
    				  properties.put(relationshipIdAsInteger, valueList);
    				  getTermToInversePropertyIdCache().put(termId, properties);
    			  }
    		  }
    	  }
      } catch (Exception e) {
    	  logWriter.error("Was not able to load inverse relationships of " + termId, e);
      }
      
      
      return term;
    } catch (JSONException je) {
      logWriter.error("bad response to getTermBySimpleName request", je);
      return null;
    }

  }

  /**
   * 
   * @param termIds - a set of termIds to be filtered against a context
   * @param context - the context to use for filtering
   * @return - the filtered list of terms
   *    no cache on client side, too many possible lists
   *    that means that, unless this context object has already filtered every one of the terms in the list, 
   *    this will cause client-server communication
   *    speed/memory tradeoff here - creating/discarding a new context every time will reduce memory usage
   *      at the expense of more client/server comm
   */
  public ArrayIntList filter(ArrayIntList termIds, Context context) { 
    ArrayIntList filtered = new ArrayIntList();
    IntIterator itr = termIds.iterator();
    while (itr.hasNext() ) {
      int candidate = itr.next();
      if (inContext(candidate, context) ) {
        filtered.add(candidate);
      }
    }
    return filtered;
  }
  
  int getInt(String name, HttpServletRequest request) throws NumberFormatException {
    String paramString = request.getParameter(name);
    if (paramString != null) {
      return Integer.parseInt(paramString.trim());
    } else {
      return -1;
    }
  }


  
  String htmlForm(ArrayIntList a_list) {
    StringBuffer paramString = new StringBuffer();
    if (a_list == null) {
      return "";
    } else {
      IntIterator itr = a_list.iterator();
      boolean first = true;
      while (itr.hasNext()) {
        if (!first) {
          paramString.append(",");
        }
        paramString.append('#'); // we're sending ids
        paramString.append(Integer.toString(itr.next()));
        first = false;  
      }
    }
    return URLEncoder.encode(paramString.toString());  
  }


  /**
   * 
   * @param termId- the termId to be tested
   * @param context - the context to test against
   * @return true if inContext, false otherwise
   * 
   * false only occurs if the term is a descendant of excludedTags
   *  this assumes each Scorer keeps a cache, so OntologyClient doesn't have to. 
   *  That way TextMap gc can get rid of inContext cache entries
   */

  public boolean inContext(int termId, Context context) { 
    String response = remoteRequest("term/read/?id="+termId+"&param=inContext"+context.toURLParams());
    try {
      JSONObject json = new JSONObject(response);
      return json.optBoolean("inContext", false);
    } catch (JSONException je) {
      logWriter.error("bad response to inContext request", je);
      return false;
    }
  }
  
  /**
   * 
   * @param id - the id of the term to be retrieved
   * @return - the Term object requested, or null
   */
  public Term getTermById(int id) { 
    Term term = termIdCache.get(id);
    if (term != null) {
      return term;
    }
   
    JSONObject json = remoteJSONRequest("term/read/?id="+id+"&relations=true");
      term = localCloneTerm(json);
      return term;
  }
  
  /**
   * 
   * @param id - the term whose ancestors are being requested
   * @return - the set of terms reachable through a simple search up a (directed)loop-free subset of the broader relation, 
   * initialized/supplemented by one step of subject relation traversal
   */
  public ArrayIntList getTermAncestors(int id) { 
    ArrayIntList ancestors = termAncestorsCache.get(id);
    if (ancestors != null) {
      return ancestors;
    }
   
    String response = remoteRequest("term/read/?param=ancestors&id="+id);
    try {
      JSONObject json = new JSONObject(response);
      JSONArray ancestorsArray = json.optJSONArray("ancestors");
      ancestors = new ArrayIntList();
      if (ancestorsArray == null) {
        return ancestors;
      }
      for (int i = 0; i < ancestorsArray.length(); i++) {
        ancestors.add(ancestorsArray.getInt(i));
      }
      termAncestorsCache.put(id, ancestors);
      return ancestors;
    } catch (JSONException je) {
      logWriter.error("bad response to getTermAncestors request", je);
      return null;
    }
  }
  
  /**
   * @param id - the term whose descendants (related with the inverse relationship "core#broader") are being requested
   * @return - the set of terms reachable through a simple search up a (directed)loop-free subset of the core#broader relation, 
   * initialized/supplemented by one step of subject relation traversal
   */
  public List<Integer> getTermDescendants(int id) {
	  // check cache first
//	  List<Integer> descendants = getTermInverseCoreBroaderCache().get(id);
	  List<Integer> descendants = null;
	  try {
		  // inverse of "broader" holds descendants
		  descendants = getTermToInversePropertyIdCache().get(id).get(broaderRelationId);
	  } catch (Throwable t) {}
	  
	  if (descendants != null) {
		  // found descendants in cache
		  return descendants;
	  }
	  // calling a query should update cache
	  getTermById(id);
	  // extract descendants again
//	  descendants = getTermInverseCoreBroaderCache().get(id);
	  try {
		  // inverse of "broader" holds descendants
		  descendants = getTermToInversePropertyIdCache().get(id).get(broaderRelationId);
	  } catch (Throwable t) {}
	  if (descendants == null || descendants.isEmpty()) {
		  // if cache was not updated, there is a problem in implementation
//		  throw new UnsupportedOperationException("Could not initialize cache of descendants when term " + id + " was queried. This may be a bug in the ontology client. Please, check jar version.");
		  return Collections.emptyList();
	  }
	  return descendants;
  }
  
  /** 
   * 
   * @param id - the term to be retrieved
   * @return - the number of term descendants of this term
   * (using the inverse of the ancestor relation described above)
   */
  public int getTermDescendantCount(int id) { 
    Integer count = termDescendantCountCache.get(id);
    if (count != null) {
      return count;
    } else {
      Term term = getTermById(id); // get sets descendantCount too
      if (term != null) {
        return termDescendantCountCache.get(id);
      } else {
        return -1;
      }
    }
  }
  
  /**
   * 
   * @param id - the term id for the subject of the query
   * @return - the depth of the term in a top-down labeling of the dag for the loop-free ancestor/descendant graph described above
   */
  public int getTermDepth(int id) { 
    Integer count = termDepthCache.get(id);
    if (count != null) {
      return count;
    } else {
      Term term = getTermById(id); // get caches depth too
      if (term != null) {
        return termDepthCache.get(id);
      } else {
        return -1;
      }
    }
  }
  
  /**
   * 
   * @param name - the exact string name of a new term. This should be unique, and should be in a separate namespace
   *   Talk to Tuuyi to get a namespace assigned, generally of the form "MyNameSpace:". This should be a prefix for all new terms
   * @return - the newly created Term
   */
  public Term createTerm(String name) {
    if (termSimpleNameCache.get(name) != null) {
      return termSimpleNameCache.get(name);
    }
    // server will only create if doesn't already exist, so this is safe
    JSONObject json = remoteJSONRequest("/term/create?name="+URLEncoder.encode(name));
    try {
      if (json == null && json.getInt("id") == -1) {
        return null;
      }
      Term term = localCloneTerm(json);
      return term;
    } catch (JSONException je) {
      logWriter.error("bad response to createTerm request", je);
      return null;
    }
  }
  
  /**
   * 
   * @param map - a TextMap - the set of terms found in a text
   * @return - the set of ancestors of the terms in the map
   *   this can be useful for finding common or unifying topics of a text
   */
  public ArrayIntList ancestors (TextMap map) {
    ArrayIntList ancestors = new ArrayIntList();
    for (LexicalFormCacheEntry lfe: map.bestInstantiation()) {
      ArrayIntList newA = getTermAncestors(lfe.getTermId());
      IntIterator itr = newA.iterator();
      while (itr.hasNext()) {
        int nextAncestor = itr.next();
        if (!ancestors.contains(nextAncestor)) {
          ancestors.add(nextAncestor);
        }
      }
    }
    return ancestors;
  }
  
  public class Scorer {
    TextMap map = null;
    ArrayIntList terms = null;
    ArrayList<Integer> ancestors = null;
    Context context = null;
    HashMap<Integer, Boolean> inContextCache = new HashMap<Integer, Boolean> ();
    
    public Scorer(TextMap map, Context a_context) {
      context = a_context;
      ArrayIntList intAncestors = ancestors(map);
      IntIterator iaItr = intAncestors.iterator();
      ancestors = new ArrayList<Integer>(intAncestors.size());
      while (iaItr.hasNext()) {
        int nextAn = iaItr.next();
        if (inContext(nextAn));
        ancestors.add(nextAn);
      }
      Collections.sort(ancestors, new DescendantComparator());
      terms = new ArrayIntList();
      for (LexicalFormCacheEntry myLfe: map.bestInstantiation()) {
        terms.add(myLfe.getTermId());
      }
    }
    
    /** asymmetric measure - estimates the degree to which this subsumes other 
     * penalizing for set difference (ie, concept space covered by this but not other)
     * @param other - a textMap
     * @param context - the context under which to look for least common parents
     * @return
     */

    public double score(TextMap other) {
      if (other == null || other.bestInstantiation() == null || other.bestInstantiation().size() == 0) {
        return 1.0;
      }
      double coverage = 1.0;
      double otherTotalPmi = 0.0;
      for (LexicalFormCacheEntry otherLfe: other.bestInstantiation()) {
        otherTotalPmi += otherLfe.getPmi();
      }
      otherLoop: for (LexicalFormCacheEntry otherLfe: other.bestInstantiation()) {
        if (!terms.contains(otherLfe.getTermId())) { // if same term, perfect coverage, ignore!
          int otherDescendantCount = getTermDescendantCount(otherLfe.getTermId())+1;
          int otherDepth = getTermDepth(otherLfe.getTermId());
          int bestLcpId = -1;
          double bestOverlap =  (1.0*otherDescendantCount)/(Term.TERM_CACHE_SIZE);
          ArrayIntList otherAncestorsInt = getTermAncestors(otherLfe.getTermId());
          ArrayList<Integer> otherAncestors = new ArrayList<Integer>();
          if (context != null) {
            otherAncestorsInt = filter(otherAncestorsInt);
          }
          otherAncestors = new ArrayList<Integer> (otherAncestorsInt.size());
          IntIterator iaItr = otherAncestorsInt.iterator(); // Collections.sort doesn't work on ArrayIntList
          while (iaItr.hasNext()) {
            int nextAn = iaItr.next();
            otherAncestors.add(nextAn);
          }
          Collections.sort(otherAncestors, new DescendantComparator());
          for (Integer my_ancestorIdInt: ancestors) {
            int my_ancestorId = my_ancestorIdInt;
            if (!otherAncestors.contains(my_ancestorId)) {
              continue;
            }
             int lcpDescendantCount = getTermDescendantCount(my_ancestorId)+1;
            int lcpDepth = getTermDepth(my_ancestorId);
            if (lcpDescendantCount <= 0) {
              lcpDescendantCount = (int)Math.max(Math.exp(Math.max(Math.max(otherDepth, lcpDepth)-lcpDepth, 1.0) ), 2.0);
              lcpDescendantCount = (int)Math.max(lcpDescendantCount,  Math.max(lcpDescendantCount, otherDescendantCount));
            }
            double overlap = (1.0*otherDescendantCount)/(lcpDescendantCount);
            if (overlap > bestOverlap) {
              bestOverlap = overlap;
              bestLcpId = my_ancestorId;
              break; // we've sorted in smallest descendant count first, so stop at first match!
            }
          }
          double lfeCoverage = Math.pow(bestOverlap,otherLfe.getPmi()/otherTotalPmi);
          coverage *= lfeCoverage;
          //coverage += Math.log((otherLfe.getPmi()/otherTotalPmi)+(1.0-(otherLfe.getPmi()/otherTotalPmi))*bestOverlap);
        }
      }
      return coverage;
      // return Math.exp(coverage-Math.log(other.bestInstantiation().size()));      return 0.0;
    }
    public boolean inContext(int termId) {
      if (inContextCache.get(termId)!= null) {
        return inContextCache.get(termId);
      }
      boolean in = OntologyClient.this.inContext(termId, context);
      inContextCache.put(termId, in);
      return in;
    }

    public ArrayIntList filter(ArrayIntList termIds) { 
      ArrayIntList filtered = new ArrayIntList();
      IntIterator itr = termIds.iterator();
      while (itr.hasNext() ) {
        int candidate = itr.next();
        if (inContext(candidate) ) {
          filtered.add(candidate);
        }
      }
      return filtered;
    }
    

  }
  
  public class DescendantComparator implements Comparator<Integer> {

    public int compare(Integer arg0, Integer arg1) {
      int count0 = getTermDescendantCount(arg0);
      int count1 = getTermDescendantCount(arg1);
      return (count0 > count1) ? 1 : (count0 < count1) ? -1 : 0;
    }
    
  }


  public static void main(String [] args) {
    OntologyClient client = null;;
    try {
      client = new OntologyClient(OntologyClient.TUUYI_SERVLET_URL);
    } catch (MalformedURLException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      System.exit(-1);
    }
    try {
      /* .../TuuyiOntologyServlet/api/term/{create, read}/?{id=termId,name=simpleName,match=simpleName} */
      Term term = client.getTermById(52354);
      ArrayIntList termAncestors = client.getTermAncestors(52354);
      client.logWriter.info("Testing get term by id: " + term.asJSON().toString());
      for (int i = 0; i < termAncestors.size(); i++) {
		term = client.getTermById(termAncestors.get(i));
		client.logWriter.info("Testing get term by id: " + term.asJSON().toString());
//		Relation relation = client.getRelation(term.getId(), client.relatedRelationId, 0);
	  }
      for (Integer descendants : client.getTermDescendants(52354)) {
    	  term = client.getTermById(descendants);
  		  client.logWriter.info("Testing descendants: " + term.asJSON().toString());
	  }
      
      term = client.getTermBySimpleName(term.getSimpleName());
      client.logWriter.info("Testing get term by simpleName: " + term.asJSON().toString());
      	try {
      		term = client.matchTermByName("Science");
      		client.logWriter.info("Testing get term: " + term.asJSON().toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
      	try {
      		term = client.getTermBySimpleName("Science");
      		client.logWriter.info("Testing get term: " + term.asJSON().toString());
      	} catch (Exception e) {
      		// TODO: handle exception
      	}
      term = client.createTerm("DigitalScirocco");
      client.logWriter.info("Testing create term: " + term.asJSON().toString());

      /* .../TuuyiOntologyServlet/api/form/{create, read, all}/?{form=aString, termId=termId,pmi=double} 
       * create and read need both a form and a termId
       * create also needs pmi
       * best and all need only form
       */
      LexicalFormCacheEntry lfe = client.getLfe("Science", 52353); // correct ID
      client.logWriter.info("Testing good get Lfe: " + lfe.asJSON().toString());
      lfe = client.getLfe("Science", 53); // correct ID
      client.logWriter.info("Testing bad get Lfe: " + lfe.asJSON().toString());
      ArrayList<LexicalFormCacheEntry> lfes = client.getLfes("Science");
      client.logWriter.info("Testing get all Lfes: " + lfes.size());
      
      /* .../TuuyiOntologyServlet/api/relation/{create, read, delete}/?term1=termId1&relation=relationId&term2=termId2 */

      /* .../TuuyiOntologyServlet/api/phrase/map?query=<string> */      
      LexicalForm.Context context = new LexicalForm.Context(null, null, null, null, null, null, 1);
//      TextMap map = client.mapText("this is a test text about science and engineering",context);
//      client.logWriter.info("Testing mapText: " + map.toString());
      
      ArrayIntList includeCats = new ArrayIntList();
      includeCats.add(client.getTermBySimpleName("Category:Science").getId());
//      includeCats.add(client.getTermBySimpleName("Category:Technology").getId());
//      includeCats.add(client.getTermBySimpleName("Category:Engineering").getId());
//      includeCats.add(client.getTermBySimpleName("Category:Medicine").getId());
      context = new LexicalForm.Context(includeCats, null, null, null, null, null, 1);
      TextMap map2 = client.mapText("this is a test text about science and engineering",context);
      client.logWriter.info("Testing mapText: " + map2.toString());
    } catch (JSONException e) {
      client.logWriter.error("error processing json", e);
    }
  }

//	/**
//	 * @return the termInverseCoreBroaderCache: cache of 
//	 * concepts inversely related to this concept by core#broader.
//	 * The key is the ID of a concept, and the value is a list
//	 * of IDs of concepts inversely related.
//	 */
//	protected Map <Integer, List<Integer>> getTermInverseCoreBroaderCache() {
//		return termInverseCoreBroaderCache;
//	}
//	
//	/**
//	 * @param termInverseCoreBroaderCache the termInverseCoreBroaderCache to set: cache of 
//	 * concepts inversely related to this concept by core#broader.
//	 * The key is the ID of a concept, and the value is a list
//	 * of IDs of concepts inversely related.
//	 */
//	protected void setTermInverseCoreBroaderCache(Map <Integer, List<Integer>> termDescendantsCache) {
//		this.termInverseCoreBroaderCache = termDescendantsCache;
//	}

	/**
	 * @return the serverURL
	 */
	public URL getServerURL() {
		return serverURL;
	}

	/**
	 * @param serverURL the serverURL to set
	 */
	public void setServerURL(URL serverURL) {
		this.serverURL = serverURL;
	}

	/**
	 *  Cache of mappings from term ID to a mapping from property ID to related terms.
     * For example, if ids of term A, B, C are respectively 1, 2, and 3; and the properties
     * skol:broader and (core:subject) are respectively 10,11, and
     * A->skol:broader->B; A->core:subject->C; 
     * B->skol:broader->C; B->core:subject->C; 
     * then the mapping will be
     * 1->{10->2, 11->3},
     * 2->{10->3, 11->3}.
	 * @return the termToPropertyIdCache
	 */
	public Map<Integer, Map<Integer,List<Integer>>> getTermToPropertyIdCache() {
		return termToPropertyIdCache;
	}

	/**
	 * Cache of mappings from term ID to a mapping from property ID to related terms.
     * For example, if ids of term A, B, C are respectively 1, 2, and 3; and the properties
     * skol:broader and (core:subject) are respectively 10,11, and
     * A->skol:broader->B; A->core:subject->C; 
     * B->skol:broader->C; B->core:subject->C; 
     * then the mapping will be
     * 1->{10->2, 11->3},
     * 2->{10->3, 11->3}.
	 * @param termToPropertyIdCache the termToPropertyIdCache to set
	 */
	public void setTermToPropertyIdCache(Map<Integer, Map<Integer,List<Integer>>> termToPropertyIdCache) {
		this.termToPropertyIdCache = termToPropertyIdCache;
	}

	/**
	 * This is similar to {@link #getTermToPropertyIdCache()}, but it stores inverse properties.
	 * @return the termToInversePropertyIdCache
	 */
	public Map<Integer, Map<Integer,List<Integer>>> getTermToInversePropertyIdCache() {
		return termToInversePropertyIdCache;
	}

	/**
	 * This is similar to {@link #setTermToPropertyIdCache(Map)}, but it stores inverse properties.
	 * @param termToInversePropertyIdCache the termToInversePropertyIdCache to set
	 */
	public void setTermToInversePropertyIdCache(
			Map<Integer, Map<Integer,List<Integer>>> termToInversePropertyIdCache) {
		this.termToInversePropertyIdCache = termToInversePropertyIdCache;
	}


}
