package com.clinica.citas;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * La arquitectura hexagonal verificada automáticamente: si alguien importa Spring en el
 * dominio, o hace que un controlador use un servicio concreto, esta prueba falla.
 */
@AnalyzeClasses(packages = "com.clinica.citas", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

    @ArchTest
    static final ArchRule el_dominio_no_depende_de_nada_externo = noClasses()
            .that().resideInAPackage("..dominio..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..aplicacion..", "..infraestructura..", "org.springframework..", "jakarta..")
            .because("el dominio es el núcleo y no debe conocer frameworks ni otras capas");

    @ArchTest
    static final ArchRule la_aplicacion_no_depende_de_la_infraestructura = noClasses()
            .that().resideInAPackage("..aplicacion..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..infraestructura..", "org.springframework..", "jakarta..")
            .because("los casos de uso solo conocen puertos (interfaces), nunca adaptadores");

    @ArchTest
    static final ArchRule los_controladores_dependen_de_puertos_y_no_de_servicios = noClasses()
            .that().resideInAPackage("..infraestructura.web..")
            .should().dependOnClassesThat().resideInAPackage("..aplicacion.servicios..")
            .because("los adaptadores de entrada usan los puertos de entrada (Inversión de Dependencias)");
}
