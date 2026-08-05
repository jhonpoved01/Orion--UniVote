package io.github.jhonpoved01.univote.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class DashboardFxmlContractTest {

    private static final String RESOURCE =
            "/io/github/jhonpoved01/univote/fxml/app-shell.fxml";

    @Test
    void dashboardExposesOnlyFunctionalMvpNavigation() throws Exception {
        List<Element> buttons = loadButtons();

        Element elections = findButton(buttons, "Elecciones y candidaturas");
        assertEquals("#handleElections", elections.getAttribute("onAction"));

        Element processDetails = findButton(buttons, "Ver detalle");
        assertEquals("#handleElections", processDetails.getAttribute("onAction"));
        assertEquals("processDetailsButton", processDetails.getAttribute("fx:id"));

        assertTrue(buttons.stream().noneMatch(button -> hasText(button, "Candidaturas")));
        assertTrue(buttons.stream().noneMatch(button -> hasText(button, "Notificaciones")));
    }

    private static List<Element> loadButtons() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        try (InputStream input = DashboardFxmlContractTest.class.getResourceAsStream(RESOURCE)) {
            assertNotNull(input, "No se encontró app-shell.fxml en el classpath");
            NodeList nodes = factory.newDocumentBuilder().parse(input).getElementsByTagName("Button");
            List<Element> buttons = new ArrayList<>(nodes.getLength());
            for (int index = 0; index < nodes.getLength(); index++) {
                buttons.add((Element) nodes.item(index));
            }
            return buttons;
        }
    }

    private static Element findButton(List<Element> buttons, String text) {
        return buttons.stream()
                .filter(button -> hasText(button, text))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el botón: " + text));
    }

    private static boolean hasText(Element button, String expected) {
        return button.getAttribute("text").strip().equals(expected);
    }
}
