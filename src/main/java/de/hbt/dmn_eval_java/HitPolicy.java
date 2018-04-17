/*
 *  ©2018 HBT Hamburger Berater Team GmbH
 *  All Rights Reserved.
 */
package de.hbt.dmn_eval_java;

/**
 * The list of <i>supported</i> hit policies (there are more, but they are not yet supported by this library).
 */
public enum HitPolicy {

    FIRST,
    UNIQUE,
    COLLECT,
    RULE_ORDER
}
