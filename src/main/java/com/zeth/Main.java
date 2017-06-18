package com.zeth;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static final String QUESTION_SPLITTER = "---------------------------------";

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        SessionFactory sessionFactory;
        sessionFactory = new Configuration().configure().buildSessionFactory();  // configures settings from hibernate.cfg.xml
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // Read XML File
        File xmlFile = new File("src/main/resources/question.cfg.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);

        doc.getDocumentElement().normalize();

        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

        String base_link = doc.getDocumentElement().getAttribute("base_link");

        NodeList nList = doc.getElementsByTagName("pack");

        for (int packIndex = 0; packIndex < nList.getLength(); packIndex++) {
            Node packNode = nList.item(packIndex);
            System.out.println("\nCurrent Element :" + packNode.getNodeName());

            if (packNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) packNode;

                System.out.println("Pack No : " + eElement.getAttribute("pack_no"));
                System.out.println("Pack Description : " + eElement.getElementsByTagName("pack_description").item(0).getTextContent());

                Question_pack pack = getQuestion_pack(session, eElement.getAttribute("pack_no"));
                pack.pack_description = eElement.getElementsByTagName("pack_description").item(0).getTextContent();
                pack.is_practice_pack = StringUtils.equalsIgnoreCase(eElement.getElementsByTagName("is_practice_pack").item(0).getTextContent(), "true");
                pack.is_test_pack = StringUtils.equalsIgnoreCase(eElement.getElementsByTagName("is_test_pack").item(0).getTextContent(), "true");

                NodeList setList = eElement.getElementsByTagName("set");

                for (int setIndex = 0; setIndex < setList.getLength(); setIndex++) {
                    Node setNode = setList.item(setIndex);

                    System.out.println("\nCurrent Element :" + setNode.getNodeName());
                    if (setNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element setElement = (Element) setNode;
                        Question_set set = getQuestion_set(session, setElement.getAttribute("set_no"));
                        set.set_description = setElement.getElementsByTagName("set_description").item(0).getTextContent();
                        set.pack_no = pack.pack_no;

                        String setURL = setElement.getElementsByTagName("link").item(0).getTextContent();
                        System.out.println(base_link+"/"+setURL);

                        List<Question> questions = extractQuestionFromLink(session, base_link+"/"+setURL, set);

                        set.questions.addAll(questions);
                        set.number_of_questions = questions.size();

                        pack.question_sets.add(set);
                    }
                }

                session.saveOrUpdate(pack);
            }

        }

        tx.commit();
        session.close();
        System.exit(0);
    }

    private static List<Question> extractQuestionFromLink(Session session, String link, Question_set set) throws IOException {
        List<Question> questions = new ArrayList<>();

        String fileContent = new String(Files.readAllBytes(Paths.get(link)));
        fileContent=fileContent.substring(fileContent.indexOf("\n")+1);

        for(String x : Arrays.stream(fileContent.split(QUESTION_SPLITTER)).map(String::trim).filter(StringUtils::isNotEmpty).collect(Collectors.toList())){
            System.out.println(x);

            String[] lines = x.split("\n");
            System.out.println(lines[0]);
            List<String> firstLine = Arrays.stream(lines[0].split("#")).map(String::trim).filter(StringUtils::isNotEmpty).collect(Collectors.toList());

            Question question = getQuestion(session, set.pack_no, set.set_no, firstLine.get(1));
            question.question_type = firstLine.get(0);
            question.correct_answer = firstLine.get(2);

            int lineIndex = 1;
            if(StringUtils.equalsAnyIgnoreCase(question.question_type, "RC")){
                question.reading_passage = lines[1];
                lineIndex++;
            }
            question.stimulus = lines[lineIndex++];
            question.option_A = trimOption(lines[lineIndex++]);
            question.option_B = trimOption(lines[lineIndex++]);
            question.option_C = trimOption(lines[lineIndex++]);
            question.option_D = trimOption(lines[lineIndex++]);
            question.option_E = trimOption(lines[lineIndex]);

            questions.add(question);
        }

        return questions;
    }

    private static String trimOption(String option){
        return option.replaceFirst("\\(.\\) ","");
    }

    private static Question_pack getQuestion_pack(Session session, String pack_no){
        Question_pack pack = session.find(Question_pack.class, pack_no);
        if(pack == null) pack = new Question_pack(pack_no);
        return pack;
    }

    private static Question_set getQuestion_set(Session session, String set_no){
        Question_set set = session.find(Question_set.class, set_no);
        if(set == null) set = new Question_set(set_no);
        return set;
    }

    private static Question getQuestion(Session session, String pack_no, String set_no, String question_no){
        String query = "SELECT e FROM Question e WHERE e.pack_no = :pack_no AND e.set_no = :set_no AND e.question_no = :question_no";

        List<Question> questions = session.createQuery(query, Question.class).setParameter("pack_no", pack_no).setParameter("set_no", set_no).setParameter("question_no", question_no)
                .list();

        if(questions.isEmpty()){
            Question question = new Question();
            question.pack_no = pack_no;
            question.set_no = set_no;
            question.question_no = question_no;

            return question;
        }
        else{
            return questions.get(0);
        }
    }
}
